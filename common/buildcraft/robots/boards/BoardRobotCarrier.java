/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robots.ai.AIRobotGotoSleep;
import buildcraft.robots.ai.AIRobotGotoStationAndUnload;
import buildcraft.robots.ai.AIRobotGotoStationToLoad;
import buildcraft.robots.ai.AIRobotLoad;
import buildcraft.robots.statements.ActionRobotFilter;

public class BoardRobotCarrier extends RedstoneBoardRobot {

	private boolean loadFound = true;
	private boolean unloadFound = true;

	public BoardRobotCarrier(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotCarrierNBT.instance;
	}

	@Override
	public void update() {
		if (!robot.containsItems()) {
			startDelegateAI(new AIRobotGotoStationToLoad(robot, ActionRobotFilter.getGateFilter(robot
					.getLinkedStation()), robot.getZoneToWork()));
		} else {
			startDelegateAI(new AIRobotGotoStationAndUnload(robot, robot.getZoneToWork()));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			if (ai.success()) {
				loadFound = true;
				startDelegateAI(new AIRobotLoad(robot, ActionRobotFilter.getGateFilter(robot
						.getLinkedStation())));
			} else {
				loadFound = false;

				if (robot.containsItems()) {
					startDelegateAI(new AIRobotGotoStationAndUnload(robot, robot.getZoneToWork()));
				} else {
					unloadFound = false;
				}
			}
		} else if (ai instanceof AIRobotGotoStationAndUnload) {
			if (ai.success()) {
				unloadFound = true;
			} else {
				unloadFound = false;
				startDelegateAI(new AIRobotGotoStationToLoad(robot, ActionRobotFilter.getGateFilter(robot
						.getLinkedStation()), robot.getZoneToWork()));
			}
		}

		if (!loadFound && !unloadFound) {
			startDelegateAI(new AIRobotGotoSleep(robot));

			// reset load and unload so that upon waking up both are tried.
			loadFound = true;
			unloadFound = true;
		}
	}
}
