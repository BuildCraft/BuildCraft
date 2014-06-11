/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

public class PathFindingJob extends Thread {

	private PathFinding pathFinding;

	public PathFindingJob(PathFinding iPathFinding) {
		super("Path Finding");
		pathFinding = iPathFinding;
	}

	@Override
	public void run() {
		while (!pathFinding.isDone()) {
			pathFinding.iterate();
		}
	}

}
