/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
 */

package net.minecraft.src.buildcraft.api;

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
