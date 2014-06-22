/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.robots.AIRobotMoveToBlock;
import buildcraft.core.utils.PathFinding;
import buildcraft.core.utils.PathFindingJob;

public class AIRobotGoToRandomDirt extends AIRobot {

	public BlockIndex dirtFound;

	private int range;
	private PathFinding pathFinding;
	private PathFindingJob pathFindingJob;

	public AIRobotGoToRandomDirt(EntityRobotBase iRobot, int iRange) {
		super(iRobot, 2, 1);

		range = iRange;
	}

	@Override
	public void update() {
		if (pathFindingJob == null) {
			double r = robot.worldObj.rand.nextFloat() * range;
			double a = robot.worldObj.rand.nextFloat() * 2.0 * Math.PI;

			int x = (int) (Math.cos(a) * r + Math.floor(robot.posX));
			int z = (int) (Math.sin(a) * r + Math.floor(robot.posZ));

			for (int y = robot.worldObj.getHeight(); y >= 0; --y) {
				Block b = robot.worldObj.getBlock(x, y, z);

				if (b instanceof BlockDirt || b instanceof BlockGrass) {
					dirtFound = new BlockIndex(x, y, z);
					pathFinding = new PathFinding(robot.worldObj, new BlockIndex(robot), dirtFound);
					pathFindingJob = new PathFindingJob(pathFinding);
					pathFindingJob.start();
					return;
				} else if (!(b instanceof BlockAir)) {
					return;
				}
			}
		} else {
			if (!pathFindingJob.isAlive()) {
				LinkedList<BlockIndex> path = pathFinding.getResult();
				path.removeLast();
				startDelegateAI(new AIRobotMoveToBlock(robot, path));
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotMoveToBlock) {
			terminate();
		}
	}

}
