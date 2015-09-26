/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.utils;

import net.minecraftforge.common.util.ForgeDirection;

public final class MatrixTransformations {
	private MatrixTransformations() {
	}

	/**
	 * Mirrors the array on the Y axis by calculating offsets from 0.5F
	 *
	 * @param targetArray
	 */
	public static void mirrorY(float[][] targetArray) {
		float temp = targetArray[1][0];
		targetArray[1][0] = 1.0F - targetArray[1][1];
		targetArray[1][1] = 1.0F - temp;
	}

	/**
	 * @param targetArray the array that should be transformed
	 * @param direction
	 */
	public static void transform(float[][] targetArray, ForgeDirection direction) {
		if ((direction.ordinal() & 0x1) == 1) {
			mirrorY(targetArray);
		}

		switch (direction.ordinal() >> 1) {
			case 1:
				for (int i = 0; i < 2; i++) {
					float temp = targetArray[2][i];
					targetArray[2][i] = targetArray[1][i];
					targetArray[1][i] = temp;
				}
				break;
			case 2:
				for (int i = 0; i < 2; i++) {
					float temp = targetArray[2][i];
					targetArray[2][i] = targetArray[0][i];
					targetArray[0][i] = targetArray[1][i];
					targetArray[1][i] = temp;
				}
				break;
		}
	}

	/**
	 * Clones both dimensions of a float[][]
	 *
	 * @param source the float[][] to deepClone
	 */
	public static float[][] deepClone(float[][] source) {
		float[][] target = source.clone();
		for (int i = 0; i < target.length; i++) {
			target[i] = source[i].clone();
		}
		return target;
	}
}
