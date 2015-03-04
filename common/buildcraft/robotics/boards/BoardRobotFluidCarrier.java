/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndLoadFluids;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotFluidCarrier extends RedstoneBoardRobot {

	public BoardRobotFluidCarrier(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotFluidCarrierNBT.instance;
	}

	@Override
	public void update() {
		startDelegateAI(new AIRobotGotoStationAndLoadFluids(robot, ActionRobotFilter.getGateFluidFilter(robot
				.getLinkedStation()), robot.getZoneToWork()));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationAndLoadFluids) {
			startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot, robot.getZoneToWork()));
		} else if (ai instanceof AIRobotGotoStationAndUnloadFluids) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}
}
