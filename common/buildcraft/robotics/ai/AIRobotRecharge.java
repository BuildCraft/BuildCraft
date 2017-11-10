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
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.IStationFilter;

public class AIRobotRecharge extends AIRobot {

	public AIRobotRecharge(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void start() {
		robot.getRegistry().releaseResources(robot);
		robot.motionX = 0;
		robot.motionY = 0;
		robot.motionZ = 0;

		startDelegateAI(new AIRobotSearchAndGotoStation(robot, new IStationFilter() {
			@Override
			public boolean matches(DockingStation station) {
				return station.providesPower();
			}
		}, null));
	}

	@Override
	public int getEnergyCost() {
		return 0;
	}

	@Override
	public void update() {
		if (robot.getEnergy() >= EntityRobotBase.MAX_ENERGY - 500) {
			terminate();
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchAndGotoStation) {
			if (!ai.success()) {
				setSuccess(false);
				terminate();
			}
		}
	}
}
