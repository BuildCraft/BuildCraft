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

	public RobotAIMoveTo(EntityRobot robot) {
		super(robot);
	}

	public RobotAIMoveTo (EntityRobot robot, float x, float y, float z) {
		super(robot);

		pathSearch = new PathFinding
				(robot.worldObj,
						new BlockIndex((int) Math.floor(robot.posX), (int) Math.floor(robot.posY),
								(int) Math.floor(robot.posZ)),
						new BlockIndex((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)));

		pathSearch.iterate(PATH_ITERATIONS);
	}

	@Override
	public void updateTask() {
		super.updateTask();

		if (path != null) {
			double distance = robot.getDistance(destX, destY, destZ);

			if (distance > prevDistance) {
				setNextInPath();
			} else {
				prevDistance = robot.getDistance(destX, destY, destZ);
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
			BlockIndex next = path.removeFirst();
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
}
