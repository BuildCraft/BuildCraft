/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robots.DockingStation;
import buildcraft.robots.IStationFilter;

public class AIRobotSearchAndGotoStation extends AIRobot {

	public DockingStation targetStation;
	private IStationFilter filter;
	private IZone zone;

	public AIRobotSearchAndGotoStation(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchAndGotoStation(EntityRobotBase iRobot, IStationFilter iFilter, IZone iZone) {
		super(iRobot);

		filter = iFilter;
		zone = iZone;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotSearchStation(robot, filter, zone));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchStation) {
			if (ai.success()) {
				targetStation = ((AIRobotSearchStation) ai).targetStation;
				startDelegateAI(new AIRobotGotoStation(robot, targetStation));
			}
		}
	}

	@Override
	public boolean success() {
		return targetStation != null;
	}
}
