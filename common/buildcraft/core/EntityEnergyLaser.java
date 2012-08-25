/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import buildcraft.api.core.Position;
import net.minecraft.src.World;

public class EntityEnergyLaser extends EntityLaser {

	public int displayStage = 0;

	private final float power [] = new float [100];
	private int powerIndex = 0;
	public float powerAverage = 0;

	public EntityEnergyLaser(World world) {
		super(world);
	}

	public EntityEnergyLaser(World world, Position head, Position tail) {
		super(world, head, tail);

		for (int j = 0; j < power.length; ++j)
			power [j] = 0;
	}

	public void pushPower (float p) {

		powerAverage -= power [powerIndex];
		powerAverage += p;
		power[powerIndex] = p;
		powerIndex++;

		if (powerIndex == power.length)
			powerIndex = 0;
	}

	public float getPowerAverage() {
		return powerAverage / power.length;
	}

	@Override
	public String getTexture () {

		if (getPowerAverage () <= 1.0)
			return DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_1.png";
		else if (getPowerAverage() <= 2.0)
			return DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_2.png";
		else if (getPowerAverage() <= 3.0)
			return DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_3.png";
		else
			return DefaultProps.TEXTURE_PATH_ENTITIES + "/laser_4.png";
	}
}
