/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

public class RobotAIDocked extends RobotAIBase {

	public RobotAIDocked(EntityRobot iRobot) {
		super(iRobot);
	}

	@Override
	public void updateTask() {
		super.updateTask();

		robot.isDocked = true;
		robot.motionX = 0;
		robot.motionY = 0;
		robot.motionZ = 0;
		robot.posX = robot.dockingStation.x + 0.5F + robot.dockingStation.side.offsetX * 0.5F;
		robot.posY = robot.dockingStation.y + 0.5F + robot.dockingStation.side.offsetY * 0.5F;
		robot.posZ = robot.dockingStation.z + 0.5F + robot.dockingStation.side.offsetZ * 0.5F;
	}
}
