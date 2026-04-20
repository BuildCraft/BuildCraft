/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.tile;

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
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.items.FluidItemDrops;
import ct.buildcraft.api.tiles.IDebuggable;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.factory.block.BlockFloodGate;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.CapUtil;
import ct.buildcraft.lib.misc.FluidUtilBC;
import ct.buildcraft.lib.misc.MessageUtil;
import ct.buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class TileFloodGate extends TileBC_Neptune implements IDebuggable {
    private static final Vec3i[] SEARCH_NORMAL = new Vec3i[] { //
        Direction.DOWN.getNormal(), Direction.NORTH.getNormal(), Direction.SOUTH.getNormal(), //
        Direction.WEST.getNormal(), Direction.EAST.getNormal() //
    };
    private static final Vec3i[] SEARCH_GASEOUS = new Vec3i[] { //
        Direction.UP.getNormal(), Direction.NORTH.getNormal(), Direction.SOUTH.getNormal(), //
        Direction.WEST.getNormal(), Direction.EAST.getNormal() //
    };

    private static final ResourceLocation ADVANCEMENT_FLOOD_SINGLE = new ResourceLocation(
        "buildcraftfactory:flooding_the_level"
    );

    private static final int[] REBUILD_DELAYS = { 16, 32, 64, 128, 256 };

    private final Tank tank = new Tank("tank", 2 * FluidType.BUCKET_VOLUME, this);
    public final Set<Direction> openSides = EnumSet.copyOf(BlockFloodGate.CONNECTED_MAP.keySet());
    public final Deque<BlockPos> queue = new ArrayDeque<>();
    private final Map<BlockPos, List<BlockPos>> paths = new HashMap<>();
    private int delayIndex = 0;
    private int tick = 0;

    public TileFloodGate(BlockPos pos, BlockState state) {
    	super(BCFactoryBlocks.ENTITYBLOCKFLOODGATE.get(), pos, state);
    	tankManager.addLast(tank);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankManager, EnumPipePart.VALUES);
        
    }

    private int getCurrentDelay() {
        return REBUILD_DELAYS[delayIndex];
    }

    private void buildQueue() {
//        level.profiler.startSection("prepare");
        queue.clear();
        paths.clear();
        FluidStack fluid = tank.getFluid();
        if (fluid == null || fluid.getAmount() <= 0) {
//            level.profiler.endSection();
            return;
        }
        Set<BlockPos> checked = new HashSet<>();
        checked.add(worldPosition);
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (Direction face : openSides) {
            BlockPos offset = worldPosition.offset(face.getNormal());
            nextPosesToCheck.add(offset);
            paths.put(offset, ImmutableList.of(offset));
        }
        Vec3i[] directions = fluid.getFluid().getFluidType().isLighterThanAir() ? SEARCH_GASEOUS : SEARCH_NORMAL;
//        level.profiler.endStartSection("build");
        outer: while (!nextPosesToCheck.isEmpty()) {
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos toCheck : nextPosesToCheckCopy) {
                if (toCheck.distSqr(worldPosition) > 64 * 64) {
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
                        for (Vec3i side : directions) {
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
//        level.profiler.endSection();
    }

    private boolean canFill(BlockPos offsetPos) {
        if (level.getBlockState(offsetPos).isAir()) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, offsetPos);
        return fluid != null && FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType())
            && BlockUtil.getFluidWithoutFlowing(getLocalState(offsetPos)) == null;
    }

    private boolean canSearch(BlockPos offsetPos) {
        if (canFill(offsetPos)) {
            return true;
        }
        Fluid fluid = BlockUtil.getFluid(level, offsetPos);
        return FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
    }

    private boolean canFillThrough(BlockPos pos) {
        if (level.getBlockState(pos).isAir()) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, pos);
        return FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
    }

    // ITickable

    @Override
    public void update() {
        if (level.isClientSide) {
            return;
        }

        if (tank.getFluidAmount() < FluidType.BUCKET_VOLUME) {
            return;
        }

        tick++;
        if (tick % 16 == 0) {
            if (!tank.isEmpty() && !queue.isEmpty()) {
                FluidStack fluid = tank.drain(FluidType.BUCKET_VOLUME, FluidAction.SIMULATE);
                if (fluid != null && fluid.getAmount() >= FluidType.BUCKET_VOLUME) {
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
//                        FakePlayer fakePlayer =
//                            BuildCraftAPI.fakePlayerProvider.getFakePlayer((WorldServer) level, getOwner(), currentPos);
                        if (FluidUtil.tryPlaceFluid(null, level, null, currentPos, tank, fluid)) {
//                            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_FLOOD_SINGLE);
                            for (Direction side : Direction.values()) {
                                level.neighborChanged(getBlockState(), currentPos.offset(side.getNormal()), BCFactoryBlocks.FLOOD_GATE_BLOCK.get(),
                                    currentPos, false);
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
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        byte b = 0;
        for (Direction face : Direction.values()) {
            if (openSides.contains(face)) {
                b |= 1 << face.get3DDataValue();
            }
        }
        nbt.putByte("openLogicalSides", b);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        Tag open = nbt.get("openLogicalSides");
        if (open instanceof NumericTag) {
            byte sides = ((NumericTag) open).getAsByte();
            for (Direction face : Direction.values()) {
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
            for (Direction face : Direction.values()) {
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
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                // tank.writeToBuffer(buffer);
                MessageUtil.writeEnumSet(buffer, openSides, Direction.class);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
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
    
	@Override
	public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
		FluidItemDrops.addFluidDrops(toDrop, tank);
		super.addDrops(toDrop, fortune);
	}

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("fluid = " + tank.getDebugString());
        left.add("open sides = " + openSides.stream().map(Enum::name).collect(Collectors.joining(", ")));
        left.add("delay = " + getCurrentDelay());
        left.add("tick = " + tick);
        left.add("queue size = " + queue.size());
    }
}
