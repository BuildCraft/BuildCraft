/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.Date;

public class PathFindingJob extends Thread {

	private PathFinding pathFinding;

	private boolean stop = false;
	private int maxIterations;

	private boolean done = false;

	public PathFindingJob(PathFinding iPathFinding, int iMaxIterations) {
		super("Path Finding");
		pathFinding = iPathFinding;
		maxIterations = iMaxIterations;
	}

	public PathFindingJob(PathFinding iPathFinding) {
		this(iPathFinding, 1000);
	}

	@Override
	public void run() {
		try {
			pathFinding.preRun();
			for (int i = 0; i < maxIterations; ++i) {
				if (isTerminated() || pathFinding.isDone()) {
					break;
				}

				long startTime = new Date().getTime();
				long elapsedtime = 0;

				pathFinding.iterate();

				elapsedtime = new Date().getTime() - startTime;
				double timeToWait = elapsedtime * 1.5;
				sleep((long) timeToWait);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			done = true;
		}
	}

	public synchronized void terminate() {
		stop = true;
	}

	public synchronized boolean isTerminated() {
		return stop;
	}

	public synchronized boolean isDone() {
		return done;
	}

}
