/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import buildcraft.api.core.Position;
import static buildcraft.core.EntityLaser.LASER_TEXTURES;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityEnergyLaser extends EntityLaser {

	public static final short POWER_AVERAGING = 100;
	public int displayStage = 0;
	private final float power[] = new float[POWER_AVERAGING];
	private int powerIndex = 0;
	private float powerAverage = 0;

	public EntityEnergyLaser(World world) {
		super(world);
	}

	public EntityEnergyLaser(World world, Position head, Position tail) {
		super(world, head, tail);
	}

	public void pushPower(float received) {

		powerAverage -= power[powerIndex];
		powerAverage += received;
		power[powerIndex] = received;
		powerIndex++;

		if (powerIndex == power.length) {
			powerIndex = 0;
		}
	}

	public float getPowerAverage() {
		return powerAverage / POWER_AVERAGING;
	}

	@Override
	public ResourceLocation getTexture() {
		if (getPowerAverage() <= 1.0)
			return LASER_TEXTURES[0];
		else if (getPowerAverage() <= 2.0)
			return LASER_TEXTURES[1];
		else if (getPowerAverage() <= 3.0)
			return LASER_TEXTURES[2];
		else
			return LASER_TEXTURES[3];
	}

	@Override
	protected void updateDataClient() {
		super.updateDataClient();
		powerAverage = (float) decodeDouble(dataWatcher.getWatchableObjectInt(15));
	}

	@Override
	protected void updateDataServer() {
		super.updateDataServer();
		dataWatcher.updateObject(15, Integer.valueOf(encodeDouble(powerAverage)));
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(15, Integer.valueOf(0));
	}
}
