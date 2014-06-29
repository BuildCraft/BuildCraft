/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.transport.PipeTransportPower;

public class AIRobotRecharge extends AIRobot {

	private DockingStation axeDocking = null;

	public AIRobotRecharge(EntityRobotBase iRobot) {
		super(iRobot, 0);
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotSearchAndGotoStation(robot, new IStationFilter() {
			@Override
			public boolean matches(DockingStation station) {
				return station.pipe.getPipeType() == PipeType.POWER;
			}
		}, null));
	}

	@Override
	public void update() {
		PipeTransportPower powerProvider = (PipeTransportPower) ((DockingStation) robot.getDockingStation()).pipe.pipe.transport;

		powerProvider.requestEnergy(robot.getDockingStation().side(), 100);
		robot.setEnergy(robot.getEnergy()
				+ powerProvider.consumePower(robot.getDockingStation().side(), 100));

		if (robot.getEnergy() >= EntityRobotBase.MAX_ENERGY) {
			terminate();
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchAndGotoStation) {
			if (robot.getDockingStation() == null
				|| !(((DockingStation) robot.getDockingStation()).pipe.pipe.transport instanceof PipeTransportPower)) {
				terminate();
			}
		}
	}
}
