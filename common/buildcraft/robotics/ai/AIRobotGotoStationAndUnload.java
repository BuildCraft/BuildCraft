/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndUnload extends AIRobot {

	private IZone zone;
	private DockingStation station;

	public AIRobotGotoStationAndUnload(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationAndUnload(EntityRobotBase iRobot, IZone iZone) {
		this(iRobot);

		zone = iZone;
	}

	public AIRobotGotoStationAndUnload(EntityRobotBase iRobot, DockingStation iStation) {
		super(iRobot);

		station = iStation;
	}

	@Override
	public void start() {
		if (station == null) {
			startDelegateAI(new AIRobotGotoStationToUnload(robot, zone));
		} else {
			startDelegateAI(new AIRobotGotoStation(robot, station));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToUnload) {
			if (ai.success()) {
				startDelegateAI(new AIRobotUnload(robot));
			} else {
				setSuccess(false);
				terminate();
			}
		} else if (ai instanceof AIRobotGotoStation) {
			if (ai.success()) {
				startDelegateAI(new AIRobotUnload(robot));
			} else {
				setSuccess(false);
				terminate();
			}
		}
	}
}
