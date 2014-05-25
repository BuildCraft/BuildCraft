/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;


public class RobotAIDirectMoveTo extends RobotAIBase {

	private double prevDistance = Double.MAX_VALUE;
	private boolean done = false;

	float x, y, z;

	public RobotAIDirectMoveTo(EntityRobot iRobot, float ix, float iy, float iz) {
		super(iRobot);
		robot = iRobot;
		x = ix;
		y = iy;
		z = iz;
	}

	@Override
	public void start() {
		setDestination(robot, x, y, z);
	}

	@Override
	public void updateTask() {
		super.updateTask();

		double distance = robot.getDistance(destX, destY, destZ);

		if (distance < prevDistance) {
			prevDistance = distance;
		} else {
			robot.motionX = 0;
			robot.motionY = 0;
			robot.motionZ = 0;

			robot.posX = x;
			robot.posY = y;
			robot.posZ = z;

			done = true;
		}
	}

	@Override
	public boolean isDone() {
		return done;
	}
}
