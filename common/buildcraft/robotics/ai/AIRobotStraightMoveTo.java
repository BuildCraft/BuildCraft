/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.robots.EntityRobotBase;

public class AIRobotStraightMoveTo extends AIRobotGoto {

	private double prevDistance = Double.MAX_VALUE;

	private float x, y, z;

	public AIRobotStraightMoveTo(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotStraightMoveTo(EntityRobotBase iRobot, float ix, float iy, float iz) {
		this(iRobot);
		x = ix;
		y = iy;
		z = iz;
		robot.aimItemAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
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

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		nbt.setFloat("x", x);
		nbt.setFloat("y", y);
		nbt.setFloat("z", z);
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("x")) {
			x = nbt.getFloat("x");
			y = nbt.getFloat("y");
			z = nbt.getFloat("z");
		}
	}
}
