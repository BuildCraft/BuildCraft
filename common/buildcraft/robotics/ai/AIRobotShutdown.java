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

public class AIRobotShutdown extends AIRobot {
	private int skip;
	private double motionX;
	private double motionZ;

	public AIRobotShutdown(EntityRobotBase iRobot) {
		super(iRobot);
		skip = 0;
		motionX = robot.motionX;
		motionZ = robot.motionZ;
	}

	@Override
	public void start() {
		robot.undock();
		robot.motionX = motionX;
		robot.motionY = -0.075f;
		robot.motionZ = motionZ;
	}

	private boolean isBlocked(float yOffset) {
		return robot.worldObj.getCollidingBoundingBoxes(robot,
				robot.getBoundingBox().addCoord(robot.motionX, yOffset, robot.motionZ)).size() > 0;
	}

	@Override
	public void update() {
		if (skip == 0) {
			if (!isBlocked(-0.075f)) {
				robot.motionY = -0.075f;
			} else {
				while (isBlocked(0)) {
					robot.posY += 0.075f;
				}
				robot.motionY = 0f;
				if (robot.motionX != 0 || robot.motionZ != 0) {
					robot.motionX = 0f;
					robot.motionZ = 0f;
					skip = 0;
				} else {
					skip = 20;
				}
			}
		} else {
			skip--;
		}
	}

	@Override
	public int getEnergyCost() {
		return 0;
	}
}
