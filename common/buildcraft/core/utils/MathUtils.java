/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class MathUtils {

	public static float clamp(float value, float min, float max) {
		return value < min ? min : (value > max ? max : value);
	}

	public static double clamp(double value, double min, double max) {
		return value < min ? min : (value > max ? max : value);
	}
}
