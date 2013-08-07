/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.core;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TickLimiter {

	public static long worldTime;
	private long lastTick = worldTime;
	private long timeSinceLastTick = -1;

	public boolean canTick() {
		if (lastTick == worldTime)
			return false;
		timeSinceLastTick = worldTime - lastTick;
		lastTick = worldTime;
		return true;
	}

	public long timeSinceLastTick() {
		return timeSinceLastTick > 0 ? timeSinceLastTick : 0;
	}
}
