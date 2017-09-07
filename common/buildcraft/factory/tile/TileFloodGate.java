/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.FakePlayer;
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
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockFloodGate;

public class TileFloodGate extends TileBC_Neptune implements ITickable, IDebuggable {
    private static final EnumFacing[] SEARCH_DIRECTIONS = new EnumFacing[] { //
        EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, //
        EnumFacing.WEST, EnumFacing.EAST //
    };

    private static final int[] REBUILD_DELAYS = { 16, 32, 64, 128, 256 };

    private final Tank tank = new Tank("tank", 2 * Fluid.BUCKET_VOLUME, this);
    public final Set<EnumFacing> openSides = EnumSet.noneOf(EnumFacing.class);
    public final Deque<BlockPos> queue = new ArrayDeque<>();
    private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();
    private int delayIndex = 0;
    private int tick = 0;

    public TileFloodGate() {
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank, EnumPipePart.VALUES);
        tankManager.add(tank);
        for (EnumFacing face : EnumFacing.VALUES) {
            if (BlockFloodGate.CONNECTED_MAP.containsKey(face)) {
                openSides.add(face);
            }
        }
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
        checked.add(pos);
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (EnumFacing face : openSides) {
            BlockPos offset = pos.offset(face);
            nextPosesToCheck.add(offset);
            paths.put(offset, ImmutableList.of(offset));
        }
        world.profiler.endStartSection("build");
        outer: while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos toCheck : nextPosesToCheckCopy) {
                if (toCheck.distanceSq(pos) > 64 * 64) {
                    continue;
                }
                if (checked.add(toCheck)) {
                    if (canSearch(toCheck)) {
                        if (canFill(toCheck)) {
                            queue.push(toCheck);
                            if (queue.size() >= 4096) {
                                break outer;
                            }
                        }
                        List<BlockPos> checkPath = paths.get(toCheck);
                        for (EnumFacing side : SEARCH_DIRECTIONS) {
                            BlockPos next = toCheck.offset(side);
                            if (checked.contains(next)) {
                                continue;
                            }
                            ImmutableList.Builder<BlockPos> pathBuilder = ImmutableList.builder();
                            pathBuilder.addAll(checkPath);
                            pathBuilder.add(next);
                            paths.put(next, pathBuilder.build());
                            nextPosesToCheck.add(next);
                        }
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
        if (fluid == null) {
            return false;
        }
        if (!FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType())) {
            // Optional.ofNullable(blockState.getBlock().getRegistryName()).map(BCModules::isBcMod).orElse(false)
            // BCModules.isBcMod(blockState.getBlock().getRegistryName());
            return false;
        }
        return BlockUtil.getFluidWithoutFlowing(getLocalState(offsetPos)) == null;
    }

    private boolean canSearch(BlockPos offsetPos) {
        if (canFill(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluid(world, offsetPos);
        return FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
    }

    private boolean canFillThrough(BlockPos pos) {
        if (world.isAirBlock(pos)) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(world, pos);
        return FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
    }

    // ITickable

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        if (tank.getFluidAmount() < Fluid.BUCKET_VOLUME) {
            return;
        }

        tick++;
        if (tick % 16 == 0) {
            if (!tank.isEmpty() && !queue.isEmpty()) {
                FluidStack fluid = tank.drain(Fluid.BUCKET_VOLUME, false);
                if (fluid != null && fluid.amount >= Fluid.BUCKET_VOLUME) {
                    BlockPos currentPos = queue.removeLast();
                    List<BlockPos> path = paths.get(currentPos);
                    boolean canFill = true;
                    if (path != null) {
                        for (BlockPos p : path) {
                            if (p.equals(currentPos)) {
                                continue;
                            }
                            if (!canFillThrough(currentPos)) {
                                canFill = false;
                                break;
                            }
                        }
                    }
                    if (canFill && canFill(currentPos)) {
                        FakePlayer fakePlayer =
                            BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) world, getOwner(), currentPos);
                        if (FluidUtil.tryPlaceFluid(fakePlayer, world, currentPos, tank, fluid)) {
                            for (EnumFacing side : EnumFacing.VALUES) {
                                world.notifyNeighborsOfStateChange(currentPos.offset(side), BCFactoryBlocks.floodGate,
                                    false);
                            }
                            delayIndex = 0;
                            tick = 0;
                        }
                    } else {
                        buildQueue();
                    }
                }
            }
        }

        if (queue.isEmpty() && tick % getCurrentDelay() == 0) {
            delayIndex = Math.min(delayIndex + 1, REBUILD_DELAYS.length - 1);
            tick = 0;
            buildQueue();
        }
    }

    // NBT

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        byte b = 0;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (openSides.contains(face)) {
                b |= 1 << face.getIndex();
            }
        }
        nbt.setByte("openSides", b);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTBase open = nbt.getTag("openSides");
        if (open instanceof NBTPrimitive) {
            byte sides = ((NBTPrimitive) open).getByte();
            for (EnumFacing face : EnumFacing.VALUES) {
                if (((sides >> face.getIndex()) & 1) == 1) {
                    openSides.add(face);
                } else {
                    openSides.remove(face);
                }
            }
        } else if (open instanceof NBTTagByteArray) {
            // Legacy: 7.99.7 and before
            byte[] bytes = ((NBTTagByteArray) open).getByteArray();
            BitSet bitSet = BitSet.valueOf(bytes);
            for (EnumFacing face : EnumFacing.VALUES) {
                if (bitSet.get(face.getIndex())) {
                    openSides.add(face);
                } else {
                    openSides.remove(face);
                }
            }
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                // tank.writeToBuffer(buffer);
                MessageUtil.writeEnumSet(buffer, openSides, EnumFacing.class);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                // tank.readFromBuffer(buffer);
                EnumSet<EnumFacing> _new = MessageUtil.readEnumSet(buffer, EnumFacing.class);
                if (!_new.equals(openSides)) {
                    openSides.clear();
                    openSides.addAll(_new);
                    redrawBlock();
                }
            }
        }
    }

    // IDebuggable

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("fluid = " + tank.getDebugString());
        String s = "";
        for (EnumFacing f : EnumFacing.VALUES) {
            if (openSides.contains(f)) {
                if (s.length() > 0) {
                    s += ", ";
                }
                s += f.getName();
            }
        }
        left.add("open sides = " + s);
        left.add("delay = " + getCurrentDelay());
        left.add("tick = " + tick);
        left.add("queue size = " + queue.size());
    }
}
