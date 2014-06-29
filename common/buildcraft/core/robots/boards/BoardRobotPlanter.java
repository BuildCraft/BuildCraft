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
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.OreStackFilter;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;
import buildcraft.core.robots.AIRobotGotoRandomGroundBlock;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotPlantSaple;
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
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new OreStackFilter("treeSapling")));
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

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoRandomGroundBlock) {
			AIRobotGotoRandomGroundBlock gotoBlock = (AIRobotGotoRandomGroundBlock) ai;

			if (((AIRobotGotoRandomGroundBlock) ai).blockFound == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				startDelegateAI(new AIRobotPlantSaple(robot, ((AIRobotGotoRandomGroundBlock) ai).blockFound));
			}
		} else if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (robot.getHeldItem() == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

}
