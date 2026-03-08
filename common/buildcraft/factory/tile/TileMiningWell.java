/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.lib.block.ILocalBlockUpdateSubscriber;
import buildcraft.lib.block.LocalBlockUpdateNotifier;
import buildcraft.lib.inventory.AutomaticProvidingTransactor;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReceiver;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

public class TileMiningWell extends TileMiner {
    private boolean shouldCheck = true;
    private final SafeTimeTracker tracker = new SafeTimeTracker(256);

    // Calen: use LocalBlockUpdateNotifier.instance(level).remove/registerSubscriberFromUpdateNotifications()
    // just like TileLaser
//    private final IWorldEventListener worldEventListener = new WorldEventListenerAdapter()
//    {
//        @Override
//        public void notifyBlockUpdate(@Nonnull World world,
//                                      @Nonnull BlockPos pos,
//                                      @Nonnull IBlockState oldState,
//                                      @Nonnull IBlockState newState,
//                                      int flags)
//        {
//            if (pos.getX() == TileMiningWell.this.pos.getX() &&
//                    pos.getY() <= TileMiningWell.this.pos.getY() &&
//                    pos.getZ() == TileMiningWell.this.pos.getZ())
//            {
//                shouldCheck = true;
//            }
//        }
//    };
    ILocalBlockUpdateSubscriber subscriber = new ILocalBlockUpdateSubscriber() {
        @Override
        public BlockPos getSubscriberPos() {
            return TileMiningWell.this.worldPosition;
        }

        @Override
        public int getUpdateRange() {
            return level.getMaxBuildHeight() - 0;
//            return level.getMaxBuildHeight() - level.getMinBuildHeight();
        }

        @Override
        public void setWorldUpdated(IWorld world, BlockPos pos) {
            if (pos.getX() == TileMiningWell.this.worldPosition.getX() &&
                    pos.getY() <= TileMiningWell.this.worldPosition.getY() &&
                    pos.getZ() == TileMiningWell.this.worldPosition.getZ())
            {
                shouldCheck = true;
            }
        }
    };

    public TileMiningWell() {
        super(BCFactoryBlocks.miningWellTile.get());
        caps.addCapabilityInstance(CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE, EnumPipePart.VALUES);
    }

    @Override
    protected void mine() {
        if (currentPos != null && canBreak()) {
            shouldCheck = true;
            long target = BlockUtil.computeBlockBreakPower(level, currentPos);
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
//                level.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
                BlockUtil.breakBlockAndGetDrops(
                        (ServerWorld) level,
                        currentPos,
                        new ItemStack(Items.DIAMOND_PICKAXE),
                        getOwner()
                ).ifPresent(stacks ->
                        stacks.forEach(stack -> InventoryUtil.addToBestAcceptor(level, worldPosition, null, stack))
                );
                nextPos();
            } else {
                if (!level.isEmptyBlock(currentPos)) {
//                    level.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                    level.destroyBlockProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                }
            }
        } else if (shouldCheck || tracker.markTimeIfDelay(level)) {
            nextPos();
            if (currentPos == null) {
                shouldCheck = false;
            }
        }
    }

    private boolean canBreak() {
        if (level.isEmptyBlock(currentPos) || BlockUtil.isUnbreakableBlock(level, currentPos, getOwner())) {
            return false;
        }

        Fluid fluid = BlockUtil.getFluidWithFlowing(level, currentPos);
        return fluid == null || fluid.getAttributes().getViscosity() <= 1000;
    }

    private void nextPos() {
        currentPos = worldPosition;
        while (true) {
            currentPos = currentPos.below();
            if (level.isOutsideBuildHeight(currentPos)) {
                break;
            }
            if (worldPosition.getY() - currentPos.getY() > BCCoreConfig.miningMaxDepth) {
                break;
            }
            if (canBreak()) {
                updateLength();
                return;
            } else if (!level.isEmptyBlock(currentPos) && level.getBlockState(currentPos).getBlock() != BCFactoryBlocks.tube.get()) {
                break;
            }
        }
        currentPos = null;
        updateLength();
    }

    @Override
//    public void validate()
    public void clearRemoved() {
//        super.validate();
        super.clearRemoved();
        if (!level.isClientSide) {
//            level.addEventListener(worldEventListener);
            LocalBlockUpdateNotifier.instance(level).registerSubscriberForUpdateNotifications(this.subscriber);
        }
    }

    @Override
//    public void invalidate()
    public void setRemoved() {
//        super.invalidate();
        super.setRemoved();
        if (!level.isClientSide) {
//            level.removeEventListener(worldEventListener);
            LocalBlockUpdateNotifier.instance(level).removeSubscriberFromUpdateNotifications(this.subscriber);
            if (currentPos != null) {
//                level.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
            }
        }
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReceiver(battery);
    }
}
