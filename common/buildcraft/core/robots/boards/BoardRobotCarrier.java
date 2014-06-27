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
import buildcraft.core.inventory.filters.PassThroughStackFilter;
import buildcraft.core.robots.AIRobotGotoStationToLoad;
import buildcraft.core.robots.AIRobotGotoStationToUnload;
import buildcraft.core.robots.AIRobotLoad;
import buildcraft.core.robots.AIRobotUnload;

public class BoardRobotCarrier extends RedstoneBoardRobot {

	public BoardRobotCarrier(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotCarrierNBT.instance;
	}

	@Override
	public void update() {
		boolean containItems = false;

		for (int i = 0; i < robot.getSizeInventory(); ++i) {
			if (robot.getStackInSlot(i) != null) {
				containItems = true;
			}
		}

		if (!containItems) {
			startDelegateAI(new AIRobotGotoStationToLoad(robot, new PassThroughStackFilter()));
		} else {
			startDelegateAI(new AIRobotGotoStationToUnload(robot));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			startDelegateAI(new AIRobotLoad(robot, new PassThroughStackFilter()));
		} else if (ai instanceof AIRobotGotoStationToUnload) {
			startDelegateAI(new AIRobotUnload(robot));
		}
	}




}
