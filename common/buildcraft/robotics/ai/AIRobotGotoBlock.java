/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import java.util.LinkedList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.IterableAlgorithmRunner;
import buildcraft.core.lib.utils.PathFinding;

public class AIRobotGotoBlock extends AIRobotGoto {

	private PathFinding pathSearch;
	private IterableAlgorithmRunner pathSearchJob;
	private LinkedList<BlockIndex> path;
	private double prevDistance = Double.MAX_VALUE;
	private float finalX, finalY, finalZ;
	private double maxDistance = 0;
	private BlockIndex lastBlockInPath;
	private boolean loadedFromNBT;

	public AIRobotGotoBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z) {
		this(robot);
		finalX = x;
		finalY = y;
		finalZ = z;
	}

	public AIRobotGotoBlock(EntityRobotBase robot, int x, int y, int z, double iMaxDistance) {
		this(robot, x, y, z);

		maxDistance = iMaxDistance;
	}

	public AIRobotGotoBlock(EntityRobotBase robot, LinkedList<BlockIndex> iPath) {
		this(robot);
		path = iPath;
		finalX = path.getLast().x;
		finalY = path.getLast().y;
		finalZ = path.getLast().z;
		setNextInPath();
	}

	@Override
	public void start() {
		robot.undock();
	}

	@Override
	public void update() {
		if (loadedFromNBT) {
			// Prevent a race condition with terminate() being called in
			// setNextInPath.
			setNextInPath();
			loadedFromNBT = false;
		}

		if (path == null && pathSearch == null) {
			pathSearch = new PathFinding(robot.worldObj, new BlockIndex((int) Math.floor(robot.posX),
					(int) Math.floor(robot.posY), (int) Math.floor(robot.posZ)), new BlockIndex(
					(int) Math.floor(finalX), (int) Math.floor(finalY), (int) Math.floor(finalZ)), maxDistance, 96);

			pathSearchJob = new IterableAlgorithmRunner(pathSearch, 50);
			pathSearchJob.start();
		} else if (path != null) {
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
			if (pathSearchJob.isDone()) {
				path = pathSearch.getResult();

				if (path.size() == 0) {
					setSuccess(false);
					terminate();
					return;
				}

				lastBlockInPath = path.getLast();

				setNextInPath();
			}
		}

		if (path != null && path.size() == 0) {
			robot.motionX = 0;
			robot.motionY = 0;
			robot.motionZ = 0;

			if (lastBlockInPath != null) {
				robot.posX = lastBlockInPath.x + 0.5F;
				robot.posY = lastBlockInPath.y + 0.5F;
				robot.posZ = lastBlockInPath.z + 0.5F;
			}
			terminate();
		}
	}

	private void setNextInPath() {
		if (path.size() > 0) {
			boolean isFirst = prevDistance == Double.MAX_VALUE;

			BlockIndex next = path.getFirst();
			prevDistance = Double.MAX_VALUE;

			if (isFirst || BuildCraftAPI.isSoftBlock(robot.worldObj, next.x, next.y, next.z)) {
				setDestination(robot, next.x + 0.5F, next.y + 0.5F, next.z + 0.5F);
				robot.aimItemAt(next.x, next.y, next.z);
			} else {
				// Path invalid!
				path = null;

				if (pathSearchJob != null) {
					pathSearchJob.terminate();
					robot.motionX = 0;
					robot.motionY = 0;
					robot.motionZ = 0;
				}
			}
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

	@Override
	public boolean canLoadFromNBT() {
		return true;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		nbt.setFloat("finalX", finalX);
		nbt.setFloat("finalY", finalY);
		nbt.setFloat("finalZ", finalZ);
		nbt.setDouble("maxDistance", maxDistance);

		if (path != null) {
			NBTTagList pathList = new NBTTagList();

			for (BlockIndex i : path) {
				NBTTagCompound subNBT = new NBTTagCompound();
				i.writeTo(subNBT);
				pathList.appendTag(subNBT);
			}

			nbt.setTag("path", pathList);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		finalX = nbt.getFloat("finalX");
		finalY = nbt.getFloat("finalY");
		finalZ = nbt.getFloat("finalZ");
		maxDistance = nbt.getDouble("maxDistance");

		if (nbt.hasKey("path")) {
			NBTTagList pathList = nbt.getTagList("path", Constants.NBT.TAG_COMPOUND);

			path = new LinkedList<BlockIndex>();

			for (int i = 0; i < pathList.tagCount(); ++i) {
				path.add(new BlockIndex(pathList.getCompoundTagAt(i)));
			}
		}

		loadedFromNBT = true;
	}
}
