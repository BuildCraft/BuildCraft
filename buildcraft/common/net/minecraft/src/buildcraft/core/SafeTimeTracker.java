package net.minecraft.src.buildcraft.core;

import net.minecraft.src.World;

public class SafeTimeTracker {

	private long lastMark = 0;
		
	/**
	 * Return true if a given delay has passed since last time marked was
	 * called successfully.
	 */
	public boolean markTimeIfDelay (World world, long delay) {
		long currentTime = world.getWorldTime();
		
		if (currentTime < lastMark) {
			lastMark = currentTime;
			return false;
		} else if (lastMark + delay <= currentTime) {
			lastMark = world.getWorldTime();
			return true;
		} else {
			return false;
		}
		
	}
	
	public void markTime (World world) {
		lastMark = world.getWorldTime();
	}
}
