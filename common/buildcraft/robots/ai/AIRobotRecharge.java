/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IPipeTile;
import buildcraft.robots.DockingStation;
import buildcraft.robots.IStationFilter;
import buildcraft.transport.PipeTransportPower;

public class AIRobotRecharge extends AIRobot {

	private DockingStation axeDocking = null;

	public AIRobotRecharge(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void start() {
		robot.getRegistry().releaseResources(robot);

		startDelegateAI(new AIRobotSearchAndGotoStation(robot, new IStationFilter() {
			@Override
			public boolean matches(DockingStation station) {
				return station.getPipe().getPipeType() == IPipeTile.PipeType.POWER;
			}
		}, null));
	}

	@Override
	public void update() {
		if (robot.getEnergy() >= EntityRobotBase.MAX_ENERGY) {
			terminate();
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchAndGotoStation) {
			if (robot.getDockingStation() == null
					|| !(((DockingStation) robot.getDockingStation()).getPipe().pipe.transport instanceof PipeTransportPower)) {
				terminate();
			}
		}
	}
}
