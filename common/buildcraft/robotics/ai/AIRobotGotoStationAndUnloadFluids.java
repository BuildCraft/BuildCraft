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
import buildcraft.api.robots.EntityRobotBase;

public class AIRobotGotoStationAndUnloadFluids extends AIRobot {

	private boolean found = false;
	private IZone zone;

	public AIRobotGotoStationAndUnloadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationAndUnloadFluids(EntityRobotBase iRobot, IZone iZone) {
		super(iRobot);

		zone = iZone;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotGotoStationToUnloadFluids(robot, zone));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToUnloadFluids) {
			if (ai.success()) {
				found = true;
				startDelegateAI(new AIRobotUnloadFluids(robot));
			} else {
				terminate();
			}
		}
	}

	@Override
	public boolean success() {
		return found;
	}
}
