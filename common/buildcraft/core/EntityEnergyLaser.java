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

	public static final short POWER_AVERAGING = 100;
	public int displayStage = 0;

	private final float power [] = new float [POWER_AVERAGING];
	private int powerIndex = 0;
	private float powerAverage = 0;

	public EntityEnergyLaser(World world) {
		super(world);
	}

	public EntityEnergyLaser(World world, Position head, Position tail) {
		super(world, head, tail);
	}

	public void pushPower (float received) {

		powerAverage -= power [powerIndex];
		powerAverage += received;
		power[powerIndex] = received;
		powerIndex++;

		if (powerIndex == power.length)
			powerIndex = 0;
	}

	public float getPowerAverage() {
		return powerAverage / POWER_AVERAGING;
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
	
	@Override
	protected void updateData() {
		super.updateData();
		
		powerAverage = (float)decodeDouble(dataWatcher.getWatchableObjectInt(15));
	}
	
	@Override
	public void setPositions(Position head, Position tail) {
		super.setPositions(head, tail);
		dataWatcher.updateObject(15, Integer.valueOf(encodeDouble((double)powerAverage)));
	}
	
	@Override
	protected void initClientSide() {
		super.initClientSide();
		dataWatcher.addObject(15, Integer.valueOf(0));
	}
	
	@Override
	protected void initServerSide() {
		super.initServerSide();
		dataWatcher.addObject(15, encodeDouble((double)powerAverage));
	}
}
