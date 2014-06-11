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

import net.minecraft.world.World;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.core.BlockIndex;
import buildcraft.core.robots.AIRobotMoveToBlock;
import buildcraft.core.utils.IPathFound;
import buildcraft.core.utils.PathFinding;
import buildcraft.robots.AIRobot;
import buildcraft.robots.EntityRobotBase;

public class AIRobotGoToWood extends AIRobot {

	public BlockIndex woodFound;
	private PathFinding woodScanner = null;

	public AIRobotGoToWood(EntityRobotBase iRobot) {
		super(iRobot, 2);
	}

	@Override
	public void start() {
		woodScanner = new PathFinding(robot.worldObj, new BlockIndex(robot), new IPathFound() {
			@Override
			public boolean endReached(World world, int x, int y, int z) {
				return BuildCraftAPI.isWoodProperty.get(world, x, y, z)
						&& !BoardRobotLumberjack.woodTargets.contains(new BlockIndex(x, y, z));
			}
		});
	}

	@Override
	public void update() {
		woodScanner.iterate(PathFinding.PATH_ITERATIONS);

		if (woodScanner.isDone()) {
			LinkedList<BlockIndex> path = woodScanner.getResult();
			woodFound = path.removeLast();
			startDelegateAI(new AIRobotMoveToBlock(robot, path));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotMoveToBlock) {
			terminate();
		}
	}
}
