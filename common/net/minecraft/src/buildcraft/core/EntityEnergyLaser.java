/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.World;

public class EntityEnergyLaser extends EntityLaser {

	public int displayStage = 0;

	private float power [] = new float [100];
	private int powerIndex = 0;
	public float powerAverage = 0;

	public void pushPower (float p) {
		powerAverage -= power [powerIndex];
		powerAverage += p;
		power [powerIndex] = p;
		powerIndex++;

		if (powerIndex == power.length)
			powerIndex = 0;
	}

	public float getPowerAverage () {
		return powerAverage / power.length;
	}

	public EntityEnergyLaser(World world) {
		super(world);

		for (int j = 0; j < power.length; ++j)
			power [j] = 0;
	}

	@Override
	public String getTexture () {
		if (getPowerAverage () <= 1.0)
			return "/net/minecraft/src/buildcraft/core/gui/laser_1.png";
		else if (getPowerAverage () <= 2.0)
			return "/net/minecraft/src/buildcraft/core/gui/laser_2.png";
		else if (getPowerAverage () <= 3.0)
			return "/net/minecraft/src/buildcraft/core/gui/laser_3.png";
		else
			return "/net/minecraft/src/buildcraft/core/gui/laser_4.png";
	}
}
