/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.filters.IFluidFilter;

public class AIRobotGotoStationAndLoadFluids extends AIRobot {

	private IFluidFilter filter;

	public AIRobotGotoStationAndLoadFluids(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoStationAndLoadFluids(EntityRobotBase iRobot, IFluidFilter iFilter) {
		this(iRobot);

		filter = iFilter;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotGotoStationToLoadFluids(robot, filter));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoadFluids) {
			if (filter != null && ai.success()) {
				startDelegateAI(new AIRobotLoadFluids(robot, filter));
			} else {
				setSuccess(false);
				terminate();
			}
		}
	}
}
