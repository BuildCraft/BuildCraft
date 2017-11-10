/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import java.util.HashSet;
import java.util.Set;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotFetchItem;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotPicker extends RedstoneBoardRobot {
	public static Set<Integer> targettedItems = new HashSet<Integer>();

	public BoardRobotPicker(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public static void onServerStart() {
		targettedItems.clear();
	}

	private void fetchNewItem() {
		startDelegateAI(new AIRobotFetchItem(robot, 250, ActionRobotFilter.getGateFilter(robot
				.getLinkedStation()), robot.getZoneToWork()));
	}

	@Override
	public void update() {
		fetchNewItem();
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotFetchItem) {
			if (ai.success()) {
				// if we find an item - that may have been cancelled.
				// let's try to get another one
				fetchNewItem();
			} else if (robot.containsItems()) {
				startDelegateAI(new AIRobotGotoStationAndUnload(robot));
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotGotoStationAndUnload) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BCBoardNBT.REGISTRY.get("picker");
	}
}
