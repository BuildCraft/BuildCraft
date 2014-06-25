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

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.utils.PathFinding;
import buildcraft.core.utils.PathFindingJob;

public class AIRobotMoveToBlock extends AIRobotMove {

	private PathFinding pathSearch;
	private PathFindingJob pathSearchJob;
	private LinkedList<BlockIndex> path;
	private double prevDistance = Double.MAX_VALUE;
	private float finalX, finalY, finalZ;

	public AIRobotMoveToBlock(EntityRobotBase robot, int x, int y, int z) {
		super(robot);
		finalX = x;
		finalY = y;
		finalZ = z;
	}

	public AIRobotMoveToBlock(EntityRobotBase robot, LinkedList<BlockIndex> iPath) {
		super(robot);
		path = iPath;
		finalX = path.getLast().x;
		finalY = path.getLast().y;
		finalZ = path.getLast().z;
		setNextInPath();
	}

	@Override
	public void start() {
		robot.undock();

		if (path == null) {
			pathSearch = new PathFinding(robot.worldObj, new BlockIndex((int) Math.floor(robot.posX),
					(int) Math.floor(robot.posY), (int) Math.floor(robot.posZ)), new BlockIndex(
					(int) Math.floor(finalX), (int) Math.floor(finalY), (int) Math.floor(finalZ)));

			pathSearchJob = new PathFindingJob(pathSearch);
			pathSearchJob.start();
		}
	}

	@Override
	public void update() {
		if (path != null) {
			double distance = robot.getDistance(nextX, nextY, nextZ);

			if (!robot.isMoving() || distance > prevDistance) {
				if (path.size() > 0) {
					path.removeFirst();
				}

				setNextInPath();
			} else {
				prevDistance = robot.getDistance(nextX, nextY, nextZ);
			}
		} else {
			if (!pathSearchJob.isAlive()) {
				if (pathSearch.isDone()) {
					path = pathSearch.getResult();
					setNextInPath();
				}
			}
		}

		if (path != null && path.size() == 0) {
			robot.motionX = 0;
			robot.motionY = 0;
			robot.motionZ = 0;
			robot.posX = finalX + 0.5F;
			robot.posY = finalY + 0.5F;
			robot.posZ = finalZ + 0.5F;

			terminate();
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
	public void end() {
		if (pathSearchJob != null) {
			pathSearchJob.terminate();
			robot.motionX = 0;
			robot.motionY = 0;
			robot.motionZ = 0;
		}
	}
}
