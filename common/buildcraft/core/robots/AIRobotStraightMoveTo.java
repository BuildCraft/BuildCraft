/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import buildcraft.api.robots.EntityRobotBase;

public class AIRobotStraightMoveTo extends AIRobotGoto {

	private double prevDistance = Double.MAX_VALUE;

	private float x, y, z;

	public AIRobotStraightMoveTo(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotStraightMoveTo(EntityRobotBase iRobot, float ix, float iy, float iz) {
		super(iRobot);
		robot = iRobot;
		x = ix;
		y = iy;
		z = iz;
	}

	@Override
	public void start() {
		robot.undock();
		setDestination(robot, x, y, z);
	}

	@Override
	public void update() {
		double distance = robot.getDistance(nextX, nextY, nextZ);

		if (distance < prevDistance) {
			prevDistance = distance;
		} else {
			robot.motionX = 0;
			robot.motionY = 0;
			robot.motionZ = 0;

			robot.posX = x;
			robot.posY = y;
			robot.posZ = z;

			terminate();
		}
	}
}
