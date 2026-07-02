/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.factory.tile;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.api.core.SafeTimeTracker;
import ct.buildcraft.api.mj.IMjReceiver;
import ct.buildcraft.core.BCCoreConfig;
import ct.buildcraft.factory.BCFactoryBlocks;
import ct.buildcraft.lib.inventory.AutomaticProvidingTransactor;
import ct.buildcraft.lib.misc.BlockUtil;
import ct.buildcraft.lib.misc.CapUtil;
import ct.buildcraft.lib.misc.InventoryUtil;
import ct.buildcraft.lib.mj.MjBatteryReceiver;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent.Message;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class TileMiningWell extends TileMiner {
	private final BlockPositionSource blockPosSource = new BlockPositionSource(this.worldPosition);
    private boolean shouldCheck = true;
    private final SafeTimeTracker tracker = new SafeTimeTracker(256);
    public final GameEventListener worldEventListener = new GameEventListener() {
    	@Override
    	public boolean handleEventsImmediately() {
    		return true;
    	}
    	@Override
    	public PositionSource getListenerSource() {
    		return blockPosSource;
    	}
    	@Override
    	public int getListenerRadius() {
    		int limt = BCCoreConfig.miningMaxDepth;
    		int high = worldPosition.getY()+64;
    		return high < limt ? high: limt;
    	}
    	@Override
    	public boolean handleGameEvent(ServerLevel p_223757_, Message msg) {
    		GameEvent e = msg.gameEvent();
    		if(e == GameEvent.BLOCK_PLACE || e == GameEvent.BLOCK_DESTROY) {
    			Vec3 pos = msg.source();
//    			BCLog.logger.debug("TileMingWell:"+msg.source()+"pos:"+
//    			(int)(pos.x - 0.5f)+" "+(int)(pos.y - 0.5f)+" "+(int)(pos.z - 0.5f));
                if ((int)(pos.x - 0.5f) == worldPosition.getX() &&
                		(int)(pos.y - 0.5f) <= worldPosition.getY() &&
                		(int)(pos.z - 0.5f) == worldPosition.getZ()) {
                        shouldCheck = true;
                    }
                return true;
    		}
    		return false;
    	}
    };

    public TileMiningWell(BlockPos pos, BlockState state) {
    	super(BCFactoryBlocks.ENTITYBLOCKMININGWELL.get(), pos, state);
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
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
                BlockUtil.breakBlockAndGetDrops(
                    (ServerLevel) level,
                    currentPos,
                    new ItemStack(Items.DIAMOND_PICKAXE),
                    getOwner()
                ).ifPresent(stacks ->
                    stacks.forEach(stack -> InventoryUtil.addToBestAcceptor(level, worldPosition, null, stack))
                );
                nextPos();
            } else {
                if (!level.getBlockState(currentPos).isAir()) {
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
        if (level.getBlockState(currentPos).isAir() || BlockUtil.isUnbreakableBlock(level, currentPos, getOwner())) {
            return false;
        }

        Fluid fluid = BlockUtil.getFluidWithFlowing(level, currentPos);
        return fluid == null || fluid.getFluidType().getViscosity() <= 1000;
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
            } else if (!level.getBlockState(currentPos).isAir() && level.getBlockState(currentPos).getBlock() != BCFactoryBlocks.TUBE_BLOCK.get()) {
                break;
            }
        }
        currentPos = null;
        updateLength();
    }

	@Override
	public void onRemove(boolean dropSelf) {
        if (!level.isClientSide) {
            if (currentPos != null) {
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
            }
        }
		super.onRemove(dropSelf);
	}

	@Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReceiver(battery);
    }
}
