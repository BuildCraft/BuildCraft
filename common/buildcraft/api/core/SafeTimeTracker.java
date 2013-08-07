/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

public class SafeTimeTracker {

	public static long worldTime;
	private long lastMark = worldTime;
	private long duration = -1;
	/**
	 * Return true if a given delay has passed since last time marked was called
	 * successfully.
	 */
	public boolean markTimeIfDelay(long delay) {
		long timePassed = worldTime - lastMark;
		if (timePassed >= delay) {
			duration = timePassed;
			lastMark = worldTime;
			return true;
		}
		if (worldTime < lastMark) {
			lastMark = worldTime;
			return false;
		}
		return false;

	}

	public long durationOfLastDelay() {
		return duration > 0 ? duration : 0;
	}

	public void markTime() {
		lastMark = worldTime;
	}
}
