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
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.utils.PathFinding;
import buildcraft.core.utils.PathFindingJob;

public class AIRobotGotoRandomGroundBlock extends AIRobot {

	public BlockIndex blockFound;

	private int range;
	private PathFinding pathFinding;
	private PathFindingJob pathFindingJob;
	private IBlockFilter filter;
	private IZone zone;

	public AIRobotGotoRandomGroundBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotGotoRandomGroundBlock(EntityRobotBase iRobot, int iRange, IBlockFilter iFilter, IZone iZone) {
		super(iRobot);

		range = iRange;
		filter = iFilter;
		zone = iZone;
	}

	@Override
	public void update() {
		if (pathFindingJob == null) {
			startDelegateAI(new AIRobotSearchRandomGroundBlock(robot, range, filter, zone));
		} else {
			if (!pathFindingJob.isAlive()) {
				LinkedList<BlockIndex> path = pathFinding.getResult();
				if (path.size() == 0) {
					terminate();
				} else {
					path.removeLast();
					startDelegateAI(new AIRobotGotoBlock(robot, path));
				}
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchRandomGroundBlock) {
			AIRobotSearchRandomGroundBlock aiFind = (AIRobotSearchRandomGroundBlock) ai;

			if (aiFind.blockFound == null) {
				terminate();
			}

			blockFound = aiFind.blockFound;
			pathFinding = new PathFinding(robot.worldObj, new BlockIndex(robot), blockFound);
			pathFindingJob = new PathFindingJob(pathFinding);
			pathFindingJob.start();
		} else if (ai instanceof AIRobotGotoBlock) {
			terminate();
		}
	}

	@Override
	public void end() {
		if (pathFindingJob != null) {
			pathFindingJob.terminate();
		}
	}
}
