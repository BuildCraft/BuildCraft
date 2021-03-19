/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fluids.Fluid;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.IMjReceiver;

import buildcraft.lib.inventory.AutomaticProvidingTransactor;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.world.WorldEventListenerAdapter;

import buildcraft.core.BCCoreConfig;
import buildcraft.factory.BCFactoryBlocks;

public class TileMiningWell extends TileMiner {
    private boolean shouldCheck = true;
    private final SafeTimeTracker tracker = new SafeTimeTracker(256);
    private final IWorldEventListener worldEventListener = new WorldEventListenerAdapter() {
        @Override
        public void notifyBlockUpdate(@Nonnull World world,
                                      @Nonnull BlockPos pos,
                                      @Nonnull IBlockState oldState,
                                      @Nonnull IBlockState newState,
                                      int flags) {
            if (pos.getX() == TileMiningWell.this.pos.getX() &&
                pos.getY() <= TileMiningWell.this.pos.getY() &&
                pos.getZ() == TileMiningWell.this.pos.getZ()) {
                shouldCheck = true;
            }
        }
    };

    public TileMiningWell() {
        super();
        caps.addCapabilityInstance(CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE, EnumPipePart.VALUES);
    }

    @Override
    protected void mine() {
        if (currentPos != null && canBreak(currentPos)) {
            shouldCheck = true;
            long target = BlockUtil.computeBlockBreakPower(world, currentPos);
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
                world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
                BlockUtil.breakBlockAndGetDrops(
                    (WorldServer) world,
                    currentPos,
                    new ItemStack(Items.DIAMOND_PICKAXE),
                    getOwner()
                ).ifPresent(stacks ->
                    stacks.forEach(stack -> InventoryUtil.addToBestAcceptor(world, pos, null, stack))
                );
                nextPos();
            } else {
                if (!world.isAirBlock(currentPos)) {
                    world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                }
            }
        } else if (shouldCheck || tracker.markTimeIfDelay(world)) {
            nextPos();
            if (currentPos == null) {
                shouldCheck = false;
            }
        }
    }

    /**
     * Examines a block to determine if the mining well can break
     * it or not.
     * 
     * @param p Position of block to examine.
     * @return
     */
    private boolean canBreak(BlockPos p) {
    	if (world.getBlockState(p).getBlockHardness(world, p) < 0) {
            return false;
        }

        Fluid fluid = BlockUtil.getFluidWithFlowing(world, p);
        return fluid == null || fluid.getViscosity() <= 1000;
    }
    
    /**
     * Examines a block to determine if the mining well drilling tip
     * can traverse it or not.
     * 
     * @param p Position of block to examine.
     * @return
     */
    private boolean canMoveThrough(BlockPos p) {
    	if (world.isAirBlock(p)) {
    		return true;
    	}
    	
    	Fluid f = BlockUtil.getFluidWithFlowing(world, p);
    	if (f == null) {
    		return false;
    	}
    	else {
    		return (f.getDensity() <= 1000);
    	}
    }
    
    /**
     * Finds the next block under the mining well which can be targeted for mining and
     * adjusts drilling pipes length accordingly.
     */
    private void nextPos() {
        currentPos = null;
        if (!world.isOutsideBuildHeight(pos)) {
	        for (int y = pos.getY() - 1; y > pos.getY() - BCCoreConfig.miningMaxDepth; y--) {
	        	BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
	        	if (canMoveThrough(p)) {
	        		continue;
	        	}
	        	if (canBreak(p)) {
	        		currentPos = p;
	        		break;
	        	}
	        	else {
	        		break;
	        	}
	        }
        }
        updateLength();
    }

    @Override
    public void validate() {
        super.validate();
        if (!world.isRemote) {
            world.addEventListener(worldEventListener);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!world.isRemote) {
            world.removeEventListener(worldEventListener);
            if (currentPos != null) {
                world.sendBlockBreakProgress(currentPos.hashCode(), currentPos, -1);
            }
        }
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReceiver(battery);
    }
}
