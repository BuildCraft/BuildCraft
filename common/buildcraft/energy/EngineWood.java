/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import buildcraft.core.DefaultProps;

public class EngineWood extends Engine {

	public EngineWood(TileEngine engine) {
		super(engine);

		maxEnergy = 1000;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_PATH_BLOCKS + "/base_wood.png";
	}

	@Override
	public int explosionRange() {
		return 1;
	}

	@Override
	public int maxEnergyReceived() {
		return 50;
	}

	@Override
	public float getPistonSpeed() {
		switch (getEnergyStage()) {
		case Blue:
			return 0.01F;
		case Green:
			return 0.02F;
		case Yellow:
			return 0.04F;
		case Red:
			return 0.08F;
		default:
			return 0;
		}
	}

	@Override
	public void update() {
		super.update();

		if (tile.isRedstonePowered) {
			if ((tile.worldObj.getWorldTime() % 20) == 0) {
				energy++;
			}
		}
	}

	@Override
	public boolean isBurning() {
		return tile.isRedstonePowered;
	}

	@Override
	public int getScaledBurnTime(int i) {
		return 0;
	}

	@Override
	public void delete() {

	}

	@Override
	public void burn() {

	}
}
