/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.factory.BCFactoryBlocks;

public class TilePump extends TileMiner {
    private final Tank tank = new Tank("tank", 16 * Fluid.BUCKET_VOLUME, this);
    private boolean queueBuilt = false;
    private Queue<BlockPos> queue = new PriorityQueue<>(
        Comparator.<BlockPos, Integer>comparing(blockPos ->
            (blockPos.getX() - pos.getX()) * (blockPos.getX() - pos.getX()) +
                (blockPos.getZ() - pos.getZ()) * (blockPos.getZ() - pos.getZ())
        ).reversed()
    );
    private Map<BlockPos, List<BlockPos>> paths = new HashMap<>();

    public TilePump() {
        tank.setCanFill(false);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank, EnumPipePart.VALUES);
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjRedstoneBatteryReceiver(battery);
    }

    private void buildQueue() {
        world.profiler.startSection("prepare");
        queue.clear();
        paths.clear();
        Set<BlockPos> checked = new HashSet<>();
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        Fluid fluid = null;
        for (BlockPos posToCheck = pos.down(); posToCheck.getY() > 0; posToCheck = posToCheck.down()) {
            if (BlockUtil.getFluidWithFlowing(world, posToCheck) != null) {
                fluid = BlockUtil.getFluidWithFlowing(world, posToCheck);
                nextPosesToCheck.add(posToCheck);
                paths.put(posToCheck, Collections.singletonList(posToCheck));
                if (BlockUtil.getFluid(world, posToCheck) != null) {
                    queue.add(posToCheck);
                }
                break;
            } else if (!world.isAirBlock(posToCheck) && world.getBlockState(posToCheck).getBlock() != BCFactoryBlocks.tube) {
                break;
            }
        }
        if (nextPosesToCheck.isEmpty()) {
            world.profiler.endSection();
            return;
        }
        world.profiler.endStartSection("build");
        while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos posToCheck : nextPosesToCheckCopy) {
                for (EnumFacing side : new EnumFacing[] {
                    EnumFacing.UP,
                    EnumFacing.NORTH,
                    EnumFacing.SOUTH,
                    EnumFacing.WEST,
                    EnumFacing.EAST
                }) {
                    BlockPos offsetPos = posToCheck.offset(side);
                    if ((offsetPos.getX() - pos.getX()) * (offsetPos.getX() - pos.getX()) +
                        (offsetPos.getZ() - pos.getZ()) * (offsetPos.getZ() - pos.getZ()) > 64 * 64) {
                        continue;
                    }
                    if (!checked.contains(offsetPos)) {
                        Fluid drain = BlockUtil.getFluidWithFlowing(world, offsetPos);
                        if (drain != null &&
                            fluid != null &&
                            Objects.equals(drain.getName(), fluid.getName())) {
                            ImmutableList.Builder<BlockPos> pathBuilder = new ImmutableList.Builder<>();
                            pathBuilder.addAll(paths.get(posToCheck));
                            pathBuilder.add(offsetPos);
                            paths.put(offsetPos, pathBuilder.build());
                            if (BlockUtil.getFluid(world, offsetPos) != null) {
                                queue.add(offsetPos);
                            }
                            nextPosesToCheck.add(offsetPos);
                        }
                        checked.add(offsetPos);
                    }
                }
            }
        }
        world.profiler.endSection();
    }

    private boolean canDrain(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluid(world, blockPos);
        return fluid != null && (tank.isEmpty() || Objects.equals(fluid.getName(), tank.getFluidType().getName()));
    }

    private void nextPos() {
        while (!queue.isEmpty()) {
            currentPos = queue.poll();
            if (canDrain(currentPos)) {
                updateLength();
                return;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
    protected void initCurrentPos() {
        if (currentPos == null) {
            nextPos();
        }
    }

    @Override
    public void update() {
        if (!queueBuilt && !world.isRemote) {
            buildQueue();
            queueBuilt = true;
        }

        super.update();

        FluidUtilBC.pushFluidAround(world, pos, tank);
    }

    @Override
    public void mine() {
        boolean prevResult = true;
        while (prevResult) {
            prevResult = false;
            if (tank.isFull()) {
                return;
            }
            long target = 10 * MjAPI.MJ;
            if (currentPos != null && paths.containsKey(currentPos)) {
                progress += battery.extractPower(0, target - progress);
                if (progress >= target) {
                    FluidStack drain = BlockUtil.drainBlock(world, currentPos, false);
                    if (drain != null &&
                        paths.get(currentPos).stream()
                            .allMatch(blockPos -> BlockUtil.getFluidWithFlowing(world, blockPos) != null) &&
                        canDrain(currentPos)) {
                        tank.fillInternal(drain, true);
                        progress = 0;
                        int count = 0;
                        if (Objects.equals(drain.getFluid().getName(), FluidRegistry.WATER.getName())) {
                            for (int x = -1; x <= 1; x++) {
                                for (int z = -1; z <= 1; z++) {
                                    BlockPos waterPos = currentPos.add(new BlockPos(x, 0, z));
                                    Fluid fluid = BlockUtil.getFluid(world, waterPos);
                                    if (fluid != null && Objects.equals(fluid.getName(), FluidRegistry.WATER.getName())) {
                                        count++;
                                    }
                                }
                            }
                        }
                        if (count < 4) {
                            BlockUtil.drainBlock(world, currentPos, true);
                            nextPos();
                        }
                    } else {
                        buildQueue();
                        nextPos();
                    }
                    prevResult = true;
                }
            } else {
                buildQueue();
                nextPos();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.deserializeNBT(nbt.getCompoundTag("tank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tank", tank.serializeNBT());
        return nbt;
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                tank.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
            } else if (id == NET_LED_STATUS) {
                tank.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = " + tank.getDebugString());
        left.add("queue size = " + queue.size());
    }
}
