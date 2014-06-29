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
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotGotoStationToLoad;
import buildcraft.core.robots.AIRobotGotoStationToUnload;
import buildcraft.core.robots.AIRobotLoad;
import buildcraft.core.robots.AIRobotUnload;

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
			startDelegateAI(new AIRobotGotoStationToLoad(robot, new PassThroughStackFilter(), robot.getAreaToWork()));
		} else {
			startDelegateAI(new AIRobotGotoStationToUnload(robot, robot.getAreaToWork()));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			if (((AIRobotGotoStationToLoad) ai).found) {
				loadFound = true;
				startDelegateAI(new AIRobotLoad(robot, new PassThroughStackFilter()));
			} else {
				loadFound = false;

				if (robot.containsItems()) {
					startDelegateAI(new AIRobotGotoStationToUnload(robot, robot.getAreaToWork()));
				} else {
					unloadFound = false;
				}
			}
		} else if (ai instanceof AIRobotGotoStationToUnload) {
			if (((AIRobotGotoStationToUnload) ai).found) {
				unloadFound = true;
				startDelegateAI(new AIRobotUnload(robot));
			} else {
				unloadFound = false;
				startDelegateAI(new AIRobotGotoStationToLoad(robot, new PassThroughStackFilter(), robot.getAreaToWork()));
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
