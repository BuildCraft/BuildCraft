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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.core.BCCoreBlocks;
import buildcraft.energy.BCEnergyFluids;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.factory.BCFactoryBlocks;

public class TilePump extends TileMiner {
    private final Tank tank = new Tank("tank", 16 * Fluid.BUCKET_VOLUME, this);
    private boolean queueBuilt = false;
    private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();
    private BlockPos fluidConnection;
    private final Queue<BlockPos> queue = new PriorityQueue<>(
        BlockUtil.uniqueBlockPosComparator(
            Comparator.<BlockPos>comparingInt(blockPos ->
                paths.get(blockPos).size()
            ).reversed()
                .thenComparing(
                    Comparator.<BlockPos>comparingInt(blockPos ->
                        (blockPos.getX() - pos.getX()) * (blockPos.getX() - pos.getX()) +
                            (blockPos.getZ() - pos.getZ()) * (blockPos.getZ() - pos.getZ())
                    ).reversed()
                )
        )
    );

    @Nullable
    private BlockPos oilSpringPos;

    public TilePump() {
        tank.setCanFill(false);
        tankManager.add(tank);
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
                fluidConnection = posToCheck;
                break;
            } else if (!world.isAirBlock(posToCheck) && world.getBlockState(posToCheck).getBlock() != BCFactoryBlocks.TUBE) {
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
                    if (checked.add(offsetPos)) {
                        if (BlockUtil.getFluidWithFlowing(world, offsetPos) == fluid) {
                            ImmutableList.Builder<BlockPos> pathBuilder = new ImmutableList.Builder<>();
                            pathBuilder.addAll(paths.get(posToCheck));
                            pathBuilder.add(offsetPos);
                            paths.put(offsetPos, pathBuilder.build());
                            if (BlockUtil.getFluid(world, offsetPos) != null) {
                                queue.add(offsetPos);
                            }
                            nextPosesToCheck.add(offsetPos);
                        }
                    }
                }
            }
        }
        world.profiler.endStartSection("oil_spring_search");
        if (FluidUtilBC.areFluidsEqual(fluid, BCEnergyFluids.crudeOil[0])) {
            List<BlockPos> springPositions = new ArrayList<>();
            BlockPos center = VecUtil.replaceValue(getPos(), Axis.Y, 0);
            for (BlockPos spring : BlockPos.getAllInBox(center.add(-10, 0, -10), center.add(10, 0, 10))) {
                if (world.getBlockState(spring).getBlock() == BCCoreBlocks.SPRING) {
                    BCLog.logger.info("Found block at " + spring);
                    TileEntity tile = world.getTileEntity(spring);
                    if (tile instanceof TileSpringOil) {
                        springPositions.add(spring);
                        BCLog.logger.info("Found a spring tile at " + spring);
                    }
                }
            }
            switch (springPositions.size()) {
                case 0:
                    break;
                case 1:
                    oilSpringPos = springPositions.get(0);
                    break;
                default:
                    springPositions.sort(Comparator.comparingDouble(pos::distanceSq));
                    oilSpringPos = springPositions.get(0);
            }

        }
        world.profiler.endSection();
    }

    private boolean canDrain(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluid(world, blockPos);
        return tank.isEmpty() ? fluid != null : fluid == tank.getFluidType();
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
    protected BlockPos getTargetPos() {
        if (currentPos != null && fluidConnection != null) {
            return currentPos.getY() > fluidConnection.getY() ? fluidConnection : currentPos;
        }
        return currentPos;
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
                        if (drain.getFluid() == FluidRegistry.WATER) {
                            for (int x = -1; x <= 1; x++) {
                                for (int z = -1; z <= 1; z++) {
                                    BlockPos waterPos = currentPos.add(new BlockPos(x, 0, z));
                                    if (BlockUtil.getFluid(world, waterPos) == FluidRegistry.WATER) {
                                        count++;
                                    }
                                }
                            }
                        }
                        if (count < 4) {
                            BlockUtil.drainBlock(world, currentPos, true);
                            if (FluidUtilBC.areFluidsEqual(drain.getFluid(), BCEnergyFluids.crudeOil[0])) {
                                if (oilSpringPos != null) {
                                    TileEntity tile = world.getTileEntity(oilSpringPos);
                                    if (tile instanceof TileSpringOil) {
                                        ((TileSpringOil) tile).onPumpOil(this, currentPos);
                                    }
                                }
                            }
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

    // NBT

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        oilSpringPos = NBTUtilBC.readBlockPos(nbt.getTag("oilSpringPos"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (oilSpringPos != null) {
            nbt.setTag("oilSpringPos", NBTUtilBC.writeBlockPos(oilSpringPos));
        }
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

    @Override
    protected long getBatteryCapacity() {
        return 50 * MjAPI.MJ;
    }
}
