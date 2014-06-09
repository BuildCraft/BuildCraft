/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.robots.DockingStation;

public class RobotAIDocked extends RobotAIBase {

	private DockingStation station;

	public RobotAIDocked(EntityRobot iRobot, DockingStation iStation) {
		super(iRobot);

		station = iStation;
	}

	@Override
	public void updateTask() {
		super.updateTask();

		robot.isDocked = true;
		robot.motionX = 0;
		robot.motionY = 0;
		robot.motionZ = 0;
		robot.posX = station.pipe.xCoord + 0.5F + station.side.offsetX * 0.5F;
		robot.posY = station.pipe.yCoord + 0.5F + station.side.offsetY * 0.5F;
		robot.posZ = station.pipe.zCoord + 0.5F + station.side.offsetZ * 0.5F;
		robot.currentDockingStation = station;
	}
}
