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
import buildcraft.core.inventory.filters.IFluidFilter;

public class AIRobotGotoStationAndLoadFluids extends AIRobot {

	private boolean found = false;
	private IZone zone;
	private IFluidFilter filter;

	public AIRobotGotoStationAndLoadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationAndLoadFluids(EntityRobotBase iRobot, IFluidFilter iFilter, IZone iZone) {
		super(iRobot);

		zone = iZone;
		filter = iFilter;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotGotoStationToLoadFluids(robot, filter, zone));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoadFluids) {
			if (ai.success()) {
				found = true;
				startDelegateAI(new AIRobotLoadFluids(robot, filter));
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
