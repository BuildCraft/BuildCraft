/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import java.util.Date;

public class IterableAlgorithmRunner extends Thread {

	private IIterableAlgorithm pathFinding;

	private boolean stop = false;
	private int maxIterations;

	private boolean done = false;

	public IterableAlgorithmRunner(IIterableAlgorithm iPathFinding, int iMaxIterations) {
		super("Path Finding");
		pathFinding = iPathFinding;
		maxIterations = iMaxIterations;
	}

	public IterableAlgorithmRunner(IIterableAlgorithm iPathFinding) {
		this(iPathFinding, 1000);
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < maxIterations; ++i) {
				if (isTerminated() || pathFinding.isDone()) {
					break;
				}

				long startTime = new Date().getTime();

				pathFinding.iterate();

				long elapsedTime = new Date().getTime() - startTime;
				int timeToWait = (int) MathUtils.clamp((int) Math.ceil(elapsedTime * 1.5), 1, 500);
				sleep(timeToWait);
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
