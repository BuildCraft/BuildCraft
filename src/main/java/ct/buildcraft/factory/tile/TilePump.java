/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.SafeTimeTracker;
import ct.buildcraft.api.items.FluidItemDrops;
import ct.buildcraft.api.mj.IMjReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.core.BCCoreBlocks;
import ct.buildcraft.core.BCCoreConfig;
import ct.buildcraft.energy.BCEnergyFluids;
import ct.buildcraft.energy.tile.ITileOilSpring;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.lib.fluid.Tank;
import ct.buildcraft.lib.misc.AdvancementUtil;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.CapUtil;
import ct.buildcraft.lib.misc.FluidUtilBC;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class TilePump extends TileMiner {
    public static final boolean DEBUG_PUMP = BCDebugging.shouldDebugComplex("factory.pump");

    private static final Direction[] SEARCH_NORMAL = new Direction[] { //
        Direction.UP, Direction.NORTH, Direction.SOUTH, //
        Direction.WEST, Direction.EAST //
    };

    private static final Direction[] SEARCH_GASEOUS = new Direction[] { //
        Direction.DOWN, Direction.NORTH, Direction.SOUTH, //
        Direction.WEST, Direction.EAST //
    };

    static final class FluidPath {
        public final BlockPos thisPos;

        @Nullable
        public final FluidPath parent;

        public FluidPath(BlockPos thisPos, FluidPath parent) {
            this.thisPos = thisPos;
            this.parent = parent;
        }

        public FluidPath and(BlockPos pos) {
            return new FluidPath(pos, this);
        }
    }

    private static final ResourceLocation ADVANCEMENT_DRAIN_ANY
        = new ResourceLocation("buildcraftfactory:draining_the_level");

    private static final ResourceLocation ADVANCEMENT_DRAIN_OIL
        = new ResourceLocation("buildcraftfactory:oil_platform");

    private final Tank tank = new Tank("tank", 16 * FluidType.BUCKET_VOLUME, this);
    private boolean queueBuilt = false;
    private final Map<BlockPos, FluidPath> paths = new HashMap<>();
    private BlockPos fluidConnection;
    private final Deque<BlockPos> queue = new ArrayDeque<>();
    private boolean isInfiniteWaterSource;
    private final SafeTimeTracker rebuildDelay = new SafeTimeTracker(30);

    /** The position just below the bottom of the pump tube. */
    private BlockPos targetPos;

    @Nullable
    private BlockPos oilSpringPos;

//	protected TankManager tankManager = new TankManager();

    public TilePump(BlockPos pos, BlockState state) {
    	super(BCFactoryBlocks.ENTITYBLOCKPUMP.get(), pos, state); 
        tank.setCanFill(false);
        tankManager.addLast(tank);
        caps.addCapabilityInstance(CapUtil.CAP_FLUIDS, tankManager, EnumPipePart.VALUES);
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjRedstoneBatteryReceiver(battery);
    }

    private void buildQueue() {
        queue.clear();
        paths.clear();
        Fluid queueFluid = Fluids.EMPTY;
        isInfiniteWaterSource = false;
        Set<BlockPos> checked = new HashSet<>();
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        for (targetPos = worldPosition.below(); !level.isOutsideBuildHeight(targetPos); targetPos = targetPos.below()) {
            if (worldPosition.getY() - targetPos.getY() > BCCoreConfig.miningMaxDepth) {
                break;
            }
            Fluid t = BlockUtil.getFluidWithFlowing(level, targetPos);//TODO check
            if (t != Fluids.EMPTY) {
                queueFluid = t;
                nextPosesToCheck.add(targetPos);
                paths.put(targetPos, new FluidPath(targetPos, null));
                checked.add(targetPos);
                if (BlockUtil.getFluid(level, targetPos) != Fluids.EMPTY) {
                    queue.add(targetPos);
                }
                fluidConnection = targetPos;
                break;
            }
            if (!level.getBlockState(targetPos).isAir() && level.getBlockState(targetPos).getBlock() != BCFactoryBlocks.TUBE_BLOCK.get()) {
                break;
            }
        }
        if (nextPosesToCheck.isEmpty() || queueFluid == Fluids.EMPTY) {
            return;
        }

//        Stopwatch watch = Stopwatch.createStarted();
        buildQueue0(queueFluid, nextPosesToCheck, checked);
//        watch.stop();
    }

    private void buildQueue0(Fluid queueFluid, List<BlockPos> nextPosesToCheck, Set<BlockPos> checked) {
        Direction[] directions = queueFluid.getFluidType().isLighterThanAir() ? SEARCH_GASEOUS : SEARCH_NORMAL;
        boolean isWater
            = !BCCoreConfig.pumpsConsumeWater && FluidUtilBC.areFluidsEqual(queueFluid, Fluids.WATER);
        final int maxDistance = Math.min(BCCoreConfig.pumpMaxDistance, 48);
        final int maxLengthSquared = maxDistance * maxDistance;
        List<BlockPos> nextPosesToCheckCopy = new ArrayList<>();
        outer: while (!nextPosesToCheck.isEmpty()) {
        	nextPosesToCheckCopy.clear();
            nextPosesToCheckCopy.addAll(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos posToCheck : nextPosesToCheckCopy) {
                int count = 0;
                for (Direction side : directions) {
                    BlockPos offsetPos = posToCheck.offset(side.getNormal());
                    if (offsetPos.distSqr(targetPos) > maxLengthSquared) {
                        continue;
                    }
                    boolean isNew = checked.add(offsetPos);
                    if (isNew) {
                        FluidState fluidsAt = level.getFluidState(offsetPos);
                        boolean eq = FluidUtilBC.areFluidsEqual(fluidsAt.getType(), queueFluid);
                        if (eq) {
                            FluidPath oldPath = paths.get(posToCheck);
                            FluidPath path = new FluidPath(offsetPos, oldPath);
                            paths.put(offsetPos, path);
                            if (fluidsAt.isSource()) {
                                queue.add(offsetPos);
                            }
                            nextPosesToCheck.add(offsetPos);
                            count++;
                        }
                    } else {
                        // We've already tested this block: it must be a valid fluid neighbour.
                        count++;
                    }
                }
                if (isWater) {
                    if (count >= 2) {
                        BlockState below = level.getBlockState(posToCheck.below());
                        // Same check as in BlockDynamicLiquid.updateTick:
                        // if that method changes how it checks for adjacent
                        // water sources then this also needs updating
                        Fluid fluidBelow = BlockUtil.getFluidWithoutFlowing(below);
                        if (
                            FluidUtilBC.areFluidsEqual(fluidBelow, Fluids.WATER) || below.getMaterial().isSolid()
                        ) {
                            isInfiniteWaterSource = true;
                            break outer;
                        }
                    }
                }
            }
        }
        if (isOil(queueFluid)) {
            List<BlockPos> springPositions = new ArrayList<>();
            BlockPos center = VecUtil.replaceValue(getBlockPos(), Axis.Y, 0);
            for (BlockPos spring : BlockPos.betweenClosed(center.offset(-10, 0, -10), center.offset(10, 0, 10))) {
                if (level.getBlockState(spring).getBlock() == BCCoreBlocks.SPRING.get()) {
                    BlockEntity tile = level.getBlockEntity(spring);
                    if (tile instanceof ITileOilSpring) {
                        springPositions.add(spring);
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
                    springPositions.sort(Comparator.comparingDouble(worldPosition::distSqr));
                    oilSpringPos = springPositions.get(0);
            }

        }
    }

    private static boolean isOil(Fluid queueFluid) {
        if (BCModules.ENERGY.isLoaded()) {
            return FluidUtilBC.areFluidsEqual(queueFluid, BCEnergyFluids.crudeOil[0]);//
        }
        return false;
    }

    private boolean canDrain(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluid(level, blockPos);
        //USE TO DEBUG
        boolean flag = tank.isEmpty() ? fluid != Fluids.EMPTY : fluid.isSource(fluid.defaultFluidState())&&FluidUtilBC.areFluidsEqual(fluid, tank.getFluidType());
        BCLog.d(flag, blockPos + " cannot drain, "+ fluid);
        return flag;    }

    private void nextPos() {
        while (!queue.isEmpty()) {
            currentPos = queue.removeLast();
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
        if (queue.isEmpty() && currentPos == null) {
            return null;
        }
        return targetPos;
    }

    @Override
    public void update() {
        if (!queueBuilt && !level.isClientSide) {
            buildQueue();
            queueBuilt = true;
        }

        super.update();

        if (!level.isClientSide) {
            FluidUtilBC.pushFluidAround(level, worldPosition, tank);
        }
    }

    @Override
    public void mine() {
        if (tank.getFluidAmount() > tank.getCapacity() / 2) {
            return;
        }
//        BCLog.logger.debug(""+currentPos);

        long target = 10 * MjAPI.MJ;
        if (currentPos != null && paths.containsKey(currentPos)) {
            progress += battery.extractPower(0, target - progress);
            if (progress < target) {
                return;
            }

            FluidStack drain = BlockUtil.drainBlock(level, currentPos, false);

            drain_attempt: {

                if (drain == FluidStack.EMPTY) {
                    if (true) {
                        BCLog.logger.info(
                            "Pump @ " + getBlockPos() + " tried to drain " + currentPos
                                + " but couldn't because no fluid was drained!"
                        );
                    }
                    break drain_attempt;
                }

                BlockPos invalid = getFirstInvalidPointOnPath(currentPos);
                if (invalid != null) {
                    if (true) {
                        BCLog.logger.info(
                            "Pump @ " + getBlockPos() + " tried to drain " + currentPos
                                + " but couldn't because the path stopped at " + invalid + "!"
                        );
                    }
                    break drain_attempt;
                } else if (!canDrain(currentPos)) {
                    if (true) {
                        BCLog.logger.info(
                            "Pump @ " + getBlockPos() + " tried to drain " + currentPos
                                + " but couldn't because it couldn't be drained!"
                        );
                    }
                    break drain_attempt;
                }
                tank.fillInternal(drain, FluidAction.EXECUTE);
                progress = 0;
                isInfiniteWaterSource &= !BCCoreConfig.pumpsConsumeWater;
                if (isInfiniteWaterSource) {
                    isInfiniteWaterSource = FluidUtilBC.areFluidsEqual(drain.getFluid(), Fluids.WATER);
                }
                AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DRAIN_ANY);
                if (!isInfiniteWaterSource) {
                    BlockUtil.drainBlock(level, currentPos, true);
                    if (isOil(drain.getFluid())) {
                        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_DRAIN_OIL);
                        if (oilSpringPos != null) {
                            BlockEntity tile = level.getBlockEntity(oilSpringPos);
                            if (tile instanceof ITileOilSpring) {
                                ((ITileOilSpring) tile).onPumpOil(getOwner(), currentPos);
                            }
                        }
                    }
                    paths.remove(currentPos);
                    nextPos();
                }
                return;
            }
            if (!rebuildDelay.markTimeIfDelay(level)) {
                return;
            }
        } else {
            if (currentPos == null && !rebuildDelay.markTimeIfDelay(level)) {
                return;
            }
            if (DEBUG_PUMP) {
                if (currentPos == null) {
                    BCLog.logger.info("Pump @ " + getBlockPos() + " is rebuilding it's queue...");
                } else {
                    BCLog.logger.info(
                        "Pump @ " + getBlockPos() + " is rebuilding it's queue because we don't have a path for "
                            + currentPos
                    );
                }
            }
        }
        buildQueue();
        nextPos();
    }
    
    public Fluid getFluidInTank() {
    	return tank.getFluidType();
    }

    @Nullable
    private BlockPos getFirstInvalidPointOnPath(BlockPos from) {
        FluidPath path = paths.get(from);
        if (path == null) {
            return from;
        }
        do {
            if (BlockUtil.getFluidWithFlowing(level, path.thisPos) == Fluids.EMPTY) {
                return path.thisPos;
            }
        } while ((path = path.parent) != null);
        return null;
    }
    
	@Override
	public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
		FluidItemDrops.addFluidDrops(toDrop, tank);
		super.addDrops(toDrop, fortune);
	}

    // NBT

    @Override
	public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (oilSpringPos != null) {
        	nbt.putLong("oilSpringPos", oilSpringPos.asLong());
        }
		nbt.put("tank", tank.serializeNBT());
        
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		oilSpringPos = BlockPos.of(nbt.getLong("oilSpringPos"));//nbt.get("oilSpringPos"));
        tank.readFromNBT(nbt.getCompound("tank"));
	}

    // Networking

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                tank.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
            } else if (id == NET_LED_STATUS) {
                tank.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = " + tank.getDebugString());
        left.add("queue size = " + queue.size());
        left.add("infinite = " + isInfiniteWaterSource);
    }

    @Override
    protected long getBatteryCapacity() {
        return 50 * MjAPI.MJ;
    }

	@Override
	public void neighbourBlockChanged(BlockState state, BlockPos neighbor, boolean harvest) {
		if(harvest) {
	        buildQueue();
	        nextPos();
	        BCLog.logger.debug("a");
		}
		super.neighbourBlockChanged(state, neighbor, harvest);
	}
    
    
}
