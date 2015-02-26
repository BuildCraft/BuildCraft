/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
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
import buildcraft.core.inventory.filters.IStackFilter;

public class AIRobotGotoStationAndLoad extends AIRobot {

	private boolean found = false;
	private IStackFilter filter;
	private IZone zone;

	public AIRobotGotoStationAndLoad(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationAndLoad(EntityRobotBase iRobot, IStackFilter iFilter, IZone iZone) {
		super(iRobot);

		filter = iFilter;
		zone = iZone;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, zone));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			if (ai.success()) {
				found = true;
				startDelegateAI(new AIRobotLoad(robot, filter, 1));
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
