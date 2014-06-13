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

public abstract class AIRobotMove extends AIRobot {

	protected float nextX, nextY, nextZ;
	protected double dirX, dirY, dirZ;

	public AIRobotMove(EntityRobotBase iRobot) {
		super(iRobot, 1);
	}

	protected void setDestination(EntityRobotBase robot, float x, float y, float z) {
		nextX = x;
		nextY = y;
		nextZ = z;

		dirX = nextX - robot.posX;
		dirY = nextY - robot.posY;
		dirZ = nextZ - robot.posZ;

		double magnitude = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);

		if (magnitude != 0) {
			dirX /= magnitude;
			dirY /= magnitude;
			dirZ /= magnitude;
		} else {
			dirX = 0;
			dirY = 0;
			dirZ = 0;
		}

		robot.motionX = dirX / 10F;
		robot.motionY = dirY / 10F;
		robot.motionZ = dirZ / 10F;
	}

}
