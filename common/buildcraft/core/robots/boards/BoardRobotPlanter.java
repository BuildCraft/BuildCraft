/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.TickHandlerCoreClient;
import buildcraft.core.inventory.filters.CompositeFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.inventory.filters.OreStackFilter;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;
import buildcraft.core.robots.AIRobotGotoRandomGroundBlock;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotSearchAndGotoBlock;
import buildcraft.core.robots.AIRobotUseToolOnBlock;
import buildcraft.core.robots.IBlockFilter;

public class BoardRobotPlanter extends RedstoneBoardRobot {

	public BoardRobotPlanter(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPlanterNBT.instance;
	}

	@Override
	public void update() {
		if (robot.getHeldItem() == null) {
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot,
					new CompositeFilter(new OreStackFilter("treeSapling"), new SeedFilter())));
		} else {
			if (robot.getHeldItem().getItem() instanceof ItemSeeds) {
				startDelegateAI(new AIRobotSearchAndGotoBlock(robot, new IBlockFilter() {
					@Override
					public boolean matches(World world, int x, int y, int z) {
						return BuildCraftAPI.isFarmlandProperty.get(world, x, y, z) && isAirAbove(world, x, y, z);
					}
				}));
			} else {
				startDelegateAI(new AIRobotGotoRandomGroundBlock(robot, 100, new IBlockFilter() {
					@Override
					public boolean matches(World world, int x, int y, int z) {
						Block b = robot.worldObj.getBlock(x, y, z);

						return b instanceof BlockDirt || b instanceof BlockGrass;
					}
				}, robot.getAreaToWork()));
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoRandomGroundBlock) {
			AIRobotGotoRandomGroundBlock gotoBlock = (AIRobotGotoRandomGroundBlock) ai;

			if (gotoBlock.blockFound == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				startDelegateAI(new AIRobotUseToolOnBlock(robot, gotoBlock.blockFound));
			}
		} else if (ai instanceof AIRobotSearchAndGotoBlock) {
			AIRobotSearchAndGotoBlock gotoBlock = (AIRobotSearchAndGotoBlock) ai;

			if (gotoBlock.blockFound == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				startDelegateAI(new AIRobotUseToolOnBlock(robot, gotoBlock.blockFound));
			}
		} else if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (robot.getHeldItem() == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

	private static class SeedFilter implements IStackFilter {
		@Override
		public boolean matches(ItemStack stack) {
			return stack.getItem() instanceof ItemSeeds;
		}
	}

	private boolean isAirAbove(World world, int x, int y, int z) {
		synchronized (TickHandlerCoreClient.startSynchronousComputation) {
			try {
				TickHandlerCoreClient.startSynchronousComputation.wait();

				return world.isAirBlock(x, y + 1, z);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}
}
