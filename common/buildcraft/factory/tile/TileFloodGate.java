/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.tiles.ITickable;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.block.BlockFloodGate;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.*;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TileFloodGate extends TileBC_Neptune implements ITickable, IDebuggable {
    private static final Direction[] SEARCH_NORMAL = new Direction[] { //
            Direction.DOWN, Direction.NORTH, Direction.SOUTH, //
            Direction.WEST, Direction.EAST //
    };
    private static final Direction[] SEARCH_GASEOUS = new Direction[] { //
            Direction.UP, Direction.NORTH, Direction.SOUTH, //
            Direction.WEST, Direction.EAST //
    };

    private static final ResourceLocation ADVANCEMENT_FLOOD_SINGLE = new ResourceLocation(
            "buildcraftfactory:flooding_the_world"
    );

    private static final int[] REBUILD_DELAYS = { 16, 32, 64, 128, 256 };

    // private final Tank tank = new Tank("tank", 2 * Fluid.BUCKET_VOLUME, this);
    private final Tank tank = new Tank("tank", 2 * FluidType.BUCKET_VOLUME, this);
    public final Set<Direction> openSides = EnumSet.copyOf(BlockFloodGate.CONNECTED_MAP.keySet());
    public final Deque<BlockPos> queue = new ArrayDeque<>();
    private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();
    private int delayIndex = 0;
    private int tick = 0;

    public TileFloodGate(BlockPos pos, BlockState blockState) {
        super(BCFactoryBlocks.floodGateTile.get(), pos, blockState);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tank, EnumPipePart.VALUES);
        tankManager.add(tank);
    }

    private int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    private void buildQueue() {
        level.getProfiler().push("prepare");
        queue.clear();
        paths.clear();
        FluidStack fluid = tank.getFluid();
//        if (fluid == null || fluid.getAmount() <= 0)
        if (fluid.isEmpty() || fluid.getAmount() <= 0) {
            level.getProfiler().pop();
            return;
        }
        Set<BlockPos> checked = new HashSet<>();
        checked.add(worldPosition);
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (Direction face : openSides) {
            BlockPos offset = worldPosition.relative(face);
            nextPosesToCheck.add(offset);
            paths.put(offset, ImmutableList.of(offset));
        }
        Direction[] directions = fluid.getFluid().getFluidType().isLighterThanAir() ? SEARCH_GASEOUS : SEARCH_NORMAL;
        level.getProfiler().popPush("build");
        outer:
        while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos toCheck : nextPosesToCheckCopy) {
//                if (toCheck.distanceSq(pos) > 64 * 64)
                if (VecUtil.distanceSq(toCheck, worldPosition) > 64 * 64) {
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
                        for (Direction side : directions) {
                            BlockPos next = toCheck.relative(side);
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
        level.getProfiler().pop();
    }

    private boolean canFill(BlockPos offsetPos) {
        if (level.isEmptyBlock(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, offsetPos);
//        return fluid != null && FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType())
        return fluid != null && FluidUtilBC.areFluidsEqualIgnoringStillOrFlow(fluid, tank.getFluidType())
                && BlockUtil.getFluidWithoutFlowing(getLocalState(offsetPos)) == null;
    }

    private boolean canSearch(BlockPos offsetPos) {
        if (canFill(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluid(level, offsetPos);
//        return FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
        return FluidUtilBC.areFluidsEqualIgnoringStillOrFlow(fluid, tank.getFluidType());
    }

    private boolean canFillThrough(BlockPos pos) {
        if (level.isEmptyBlock(pos)) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, pos);
//        return FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
        return FluidUtilBC.areFluidsEqualIgnoringStillOrFlow(fluid, tank.getFluidType());
    }

    // ITickable

    @Override
    public void update() {
        ITickable.super.update();
        if (level.isClientSide) {
            return;
        }

//        if (tank.getFluidAmount() < Fluid.BUCKET_VOLUME)
        if (tank.getFluidAmount() < FluidType.BUCKET_VOLUME) {
            return;
        }

        tick++;
        if (tick % 16 == 0) {
            if (!tank.isEmpty() && !queue.isEmpty()) {
//                FluidStack fluid = tank.drain(Fluid.BUCKET_VOLUME, false);
                FluidStack fluid = tank.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
//                if (fluid != null && fluid.getAmount() >= Fluid.BUCKET_VOLUME)
                if (!fluid.isEmpty() && fluid.getAmount() >= FluidType.BUCKET_VOLUME) {
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
                                BuildCraftAPI.fakePlayerProvider.getFakePlayer((ServerLevel) level, getOwner(), currentPos);
//                        if (FluidUtil.tryPlaceFluid(fakePlayer, level, currentPos, tank, fluid))
                        if (FluidUtil.tryPlaceFluid(fakePlayer, level, InteractionHand.MAIN_HAND, currentPos, tank, fluid)) {
                            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_FLOOD_SINGLE);
                            for (Direction side : Direction.VALUES) {
//                                level.notifyNeighborsOfStateChange(currentPos.offset(side), BCFactoryBlocks.floodGate, false);
                                level.updateNeighborsAt(currentPos.relative(side), BCFactoryBlocks.floodGate.get());
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

        if (queue.isEmpty() && tick >= getCurrentDelay()) {
            delayIndex = Math.min(delayIndex + 1, REBUILD_DELAYS.length - 1);
            tick = 0;
            buildQueue();
        }
    }

    // NBT

    @Override
//    public CompoundTag writeToNBT(CompoundTag nbt)
    public void saveAdditional(CompoundTag nbt) {
//        super.writeToNBT(nbt);
        super.saveAdditional(nbt);
        byte b = 0;
        for (Direction face : Direction.VALUES) {
            if (openSides.contains(face)) {
                b |= 1 << face.get3DDataValue();
            }
        }
        nbt.putByte("openSides", b);
//        return nbt;
    }

    @Override
//    public void readFromNBT(CompoundTag nbt)
    public void load(CompoundTag nbt) {
//        super.readFromNBT(nbt);
        super.load(nbt);
        Tag open = nbt.get("openSides");
//        if (open instanceof NBTPrimitive)
        if (open instanceof NumericTag) {
//            byte sides = ((NBTPrimitive) open).getByte();
            byte sides = ((NumericTag) open).getAsByte();
            for (Direction face : Direction.VALUES) {
//                if (((sides >> face.getIndex()) & 1) == 1)
                if (((sides >> face.get3DDataValue()) & 1) == 1) {
                    openSides.add(face);
                } else {
                    openSides.remove(face);
                }
            }
        } else if (open instanceof ByteArrayTag) {
            // Legacy: 7.99.7 and before
            byte[] bytes = ((ByteArrayTag) open).getAsByteArray();
            BitSet bitSet = BitSet.valueOf(bytes);
            for (Direction face : Direction.VALUES) {
//                if (bitSet.get(face.getIndex()))
                if (bitSet.get(face.get3DDataValue())) {
                    openSides.add(face);
                } else {
                    openSides.remove(face);
                }
            }
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_RENDER_DATA) {
                // tank.writeToBuffer(buffer);
                MessageUtil.writeEnumSet(buffer, openSides, Direction.class);
            }
        }
    }

    @Override
//    public void readPayload(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_RENDER_DATA) {
                // tank.readFromBuffer(buffer);
                EnumSet<Direction> _new = MessageUtil.readEnumSet(buffer, Direction.class);
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
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//        left.add("fluid = " + tank.getDebugString());
//        left.add("open sides = " + openSides.stream().map(Enum::name).collect(Collectors.joining(", ")));
//        left.add("delay = " + getCurrentDelay());
//        left.add("tick = " + tick);
//        left.add("queue size = " + queue.size());
        left.add(Component.literal("fluid = " + tank.getDebugString()));
        left.add(Component.literal("open sides = " + openSides.stream().map(Enum::name).collect(Collectors.joining(", "))));
        left.add(Component.literal("delay = " + getCurrentDelay()));
        left.add(Component.literal("tick = " + tick));
        left.add(Component.literal("queue size = " + queue.size()));
    }
}
