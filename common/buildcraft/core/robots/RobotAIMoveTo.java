/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.core.BlockIndex;
import buildcraft.core.utils.PathFinding;

public class RobotAIMoveTo extends RobotAIBase {

	private static int PATH_ITERATIONS = 1000;

	private PathFinding pathSearch;
	private LinkedList<BlockIndex> path;
	private double prevDistance = Double.MAX_VALUE;
	private float dx, dy, dz;

	public RobotAIMoveTo (EntityRobot robot, float x, float y, float z) {
		super(robot);
		dx = x;
		dy = y;
		dz = z;
	}

	@Override
	public void updateTask() {
		super.updateTask();

		if (pathSearch == null) {
			pathSearch = new PathFinding
					(robot.worldObj,
							new BlockIndex((int) Math.floor(robot.posX), (int) Math.floor(robot.posY),
									(int) Math.floor(robot.posZ)),
							new BlockIndex((int) Math.floor(dx), (int) Math.floor(dy), (int) Math.floor(dz)));

			pathSearch.iterate(PATH_ITERATIONS);
		} else if (path != null) {
			double distance = robot.getDistance(destX, destY, destZ);

			if (distance > prevDistance) {
				if (path.size() > 0) {
					path.removeFirst();
				}

				setNextInPath();
			} else {
				prevDistance = robot.getDistance(destX, destY, destZ);
			}

			if (path.size() == 0) {
				robot.motionX = 0;
				robot.motionY = 0;
				robot.motionZ = 0;
			}
		} else if (!pathSearch.isDone()) {
			pathSearch.iterate(PATH_ITERATIONS);
		} else {
			path = pathSearch.getResult();
			setNextInPath();
		}
	}

	private void setNextInPath() {
		if (path.size() > 0) {
			BlockIndex next = path.getFirst();
			setDestination(robot, next.x + 0.5F, next.y + 0.5F, next.z + 0.5F);
			prevDistance = Double.MAX_VALUE;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
    }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public boolean isDone() {
		return path != null && path.size() == 0;
	}
}
