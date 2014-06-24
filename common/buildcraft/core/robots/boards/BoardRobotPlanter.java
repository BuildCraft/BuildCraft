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
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.OreStackFilter;

public class BoardRobotPlanter extends RedstoneBoardRobot {

	public BoardRobotPlanter(EntityRobotBase iRobot) {
		super(iRobot, 1);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPlanterNBT.instance;
	}

	@Override
	public void update() {
		if (robot.getItemInUse() == null) {
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new OreStackFilter("treeSapling")));
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
