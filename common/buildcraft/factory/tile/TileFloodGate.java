/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Booleans;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockFloodGate;

public class TileFloodGate extends TileBC_Neptune implements ITickable, IDebuggable {
    private static final int[] REBUILD_DELAYS = new int[] {
        1,
        2,
        4,
        8,
        16,
        32,
        64,
        128,
        256,
        512,
        1024,
        2048,
        4096,
        8192,
        16384
    };

    private final Tank tank = new Tank("tank", 2 * Fluid.BUCKET_VOLUME, this);
    public final EnumMap<EnumFacing, Boolean> openSides = new EnumMap<>(EnumFacing.class);
    public final Queue<BlockPos> queue = new PriorityQueue<>(
        Comparator.<BlockPos>comparingInt(blockPos ->
            (blockPos.getX() - pos.getX()) * (blockPos.getX() - pos.getX()) +
                (blockPos.getY() - pos.getY()) * (blockPos.getY() - pos.getY()) +
                (blockPos.getZ() - pos.getZ()) * (blockPos.getZ() - pos.getZ())
        )
    );
    private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();
    private int delayIndex = 0;
    private int tick = 0;

    public TileFloodGate() {
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank, EnumPipePart.VALUES);
        Arrays.stream(EnumFacing.VALUES)
            .forEach(side -> openSides.put(side, BlockFloodGate.CONNECTED_MAP.containsKey(side)));
    }

    private int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    private void buildQueue() {
        world.profiler.startSection("prepare");
        queue.clear();
        paths.clear();
        if (tank.isEmpty()) {
            world.profiler.endSection();
            return;
        }
        Set<BlockPos> checked = new HashSet<>();
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        openSides.entrySet().stream()
            .filter(Entry::getValue)
            .map(Entry::getKey)
            .map(pos::offset)
            .forEach(nextPosesToCheck::add);
        world.profiler.endStartSection("build");
        outer:
        while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos posToCheck : nextPosesToCheckCopy) {
                for (EnumFacing side : new EnumFacing[] {
                    EnumFacing.DOWN,
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
                    if (!openSides.get(side)) {
                        if (side == EnumFacing.NORTH && offsetPos.getZ() >= pos.getZ()) {
                            continue;
                        }
                        if (side == EnumFacing.SOUTH && offsetPos.getZ() <= pos.getZ()) {
                            continue;
                        }
                        if (side == EnumFacing.WEST && offsetPos.getX() >= pos.getX()) {
                            continue;
                        }
                        if (side == EnumFacing.EAST && offsetPos.getX() <= pos.getX()) {
                            continue;
                        }
                    }
                    if (!checked.contains(offsetPos)) {
                        if (canSearch(offsetPos)) {
                            ImmutableList.Builder<BlockPos> pathBuilder = new ImmutableList.Builder<>();
                            if (paths.containsKey(posToCheck)) {
                                pathBuilder.addAll(paths.get(posToCheck));
                            }
                            pathBuilder.add(offsetPos);
                            paths.put(offsetPos, pathBuilder.build());
                            if (canFill(offsetPos)) {
                                if (openSides.get(EnumFacing.DOWN) ||
                                    Math.abs(offsetPos.getY() - pos.getY()) < Math.abs(offsetPos.getX() - pos.getX()) ||
                                    Math.abs(offsetPos.getY() - pos.getY()) < Math.abs(offsetPos.getZ() - pos.getZ())) {
                                    queue.add(offsetPos);
                                }
                                if (queue.size() >= 4096) {
                                    break outer;
                                }
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

    private boolean canFill(BlockPos offsetPos) {
        if (world.isAirBlock(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, offsetPos);
        // noinspection RedundantIfStatement
        if (fluid != null &&
            Objects.equals(fluid.getName(), tank.getFluidType().getName()) &&
            BlockUtil.getFluid(world, offsetPos) == null) {
            return true;
        }
        return false;
    }

    private boolean canSearch(BlockPos offsetPos) {
        if (canFill(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluid(world, offsetPos);
        return fluid != null && Objects.equals(fluid.getName(), tank.getFluidType().getName());
    }

    // ITickable

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

//        tank.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
        FluidUtilBC.pullFluidAround(world, pos, tank);

        tick++;
        if (tick % 16 == 0) {
            if (!tank.isEmpty() && !queue.isEmpty()) {
                FluidStack fluid = tank.drain(Fluid.BUCKET_VOLUME, false);
                if (fluid != null && fluid.amount >= Fluid.BUCKET_VOLUME) {
                    BlockPos currentPos = queue.poll();
                    if (paths.get(currentPos).stream().allMatch(this::canSearch) && canFill(currentPos)) {
                        if (FluidUtil.tryPlaceFluid(
                            BuildCraftAPI.fakePlayerProvider.getFakePlayer(
                                (WorldServer) world,
                                getOwner(),
                                currentPos
                            ),
                            world,
                            currentPos,
                            tank,
                            fluid
                        )) {
                            for (EnumFacing side : EnumFacing.VALUES) {
                                world.notifyNeighborsOfStateChange(
                                    currentPos.offset(side),
                                    BCFactoryBlocks.floodGate,
                                    false
                                );
                            }
                            delayIndex = 0;
                        }
                    } else {
                        buildQueue();
                    }
                }
            }
        }

        if (queue.isEmpty() && tick % getCurrentDelay() == 0) {
            delayIndex = Math.min(delayIndex + 1, REBUILD_DELAYS.length - 1);
            buildQueue();
        }
    }

    // NBT

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        nbt.setTag("openSides", NBTUtilBC.writeBooleanList(openSides.values().stream()));
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt.getCompoundTag("tank"));
        Boolean[] blockedSidesArray = NBTUtilBC.readBooleanList(nbt.getTag("openSides")).toArray(Boolean[]::new);
        for (int i = 0; i < blockedSidesArray.length; i++) {
            openSides.put(EnumFacing.getFront(i), blockedSidesArray[i]);
        }
    }

    // Netwokring

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                // tank.writeToBuffer(buffer);
                MessageUtil.writeBooleanArray(buffer, Booleans.toArray(openSides.values()));
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                // tank.readFromBuffer(buffer);
                boolean[] old = Booleans.toArray(openSides.values());
                boolean[] blockedSidesArray = MessageUtil.readBooleanArray(buffer, openSides.values().size());
                for (int i = 0; i < blockedSidesArray.length; i++) {
                    openSides.put(EnumFacing.getFront(i), blockedSidesArray[i]);
                }
                if (!Arrays.equals(old, Booleans.toArray(openSides.values()))) {
                    redrawBlock();
                }
            }
        }
    }

    // IDebuggable

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("fluid = " + tank.getDebugString());
        left.add("open sides = " + openSides.entrySet().stream()
            .filter(Entry::getValue)
            .map(Entry::getKey)
            .map(Enum::name)
            .collect(Collectors.joining(", ")));
        left.add("delay = " + getCurrentDelay());
        left.add("tick = " + tick);
        left.add("queue size = " + queue.size());
    }
}
