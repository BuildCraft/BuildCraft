/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.inventory.filters.OreStackFilter;
import buildcraft.robots.AIRobot;
import buildcraft.robots.EntityRobotBase;

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
		if (robot.getItemInUse() == null) {
			startDelegateAI(new AIRobotFetchItemStack(robot, new OreStackFilter("treeSapling")));
		} else {
			startDelegateAI(new AIRobotGoToRandomDirt(robot, 100));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGoToRandomDirt) {
			startDelegateAI(new AIRobotPlantSaple(robot, ((AIRobotGoToRandomDirt) ai).dirtFound));
		}
	}

}
