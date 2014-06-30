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
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.utils.PathFinding;
import buildcraft.core.utils.PathFindingJob;

public class AIRobotSearchAndGotoBlock extends AIRobot {

	public BlockIndex blockFound;
	private PathFinding blockScanner = null;
	private PathFindingJob blockScannerJob;
	private IBlockFilter pathFound;
	private int stopBefore = 0;

	public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotSearchAndGotoBlock(EntityRobotBase iRobot, IBlockFilter iPathFound) {
		super(iRobot);

		pathFound = iPathFound;
		stopBefore = 0;
	}

	@Override
	public void start() {
		blockScanner = new PathFinding(robot.worldObj, new BlockIndex(robot), pathFound, 64, robot.getAreaToWork());
		blockScannerJob = new PathFindingJob(blockScanner);
		blockScannerJob.start();
	}

	@Override
	public void update() {
		if (blockScannerJob.isDone()) {
			LinkedList<BlockIndex> path = blockScanner.getResult();

			if (path != null) {
				blockFound = path.removeLast();
				startDelegateAI(new AIRobotGotoBlock(robot, path));
			} else {
				terminate();
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoBlock) {
			terminate();
		}
	}

	@Override
	public void end() {
		if (blockScannerJob != null) {
			blockScannerJob.terminate();
		}
	}
}
