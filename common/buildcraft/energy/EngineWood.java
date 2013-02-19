/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidStack;
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
	public int minEnergyReceived() {
		return 1;
	}

	@Override
	public int maxEnergyReceived() {
		return 50;
	}

	@Override
	public float getPistonSpeed() {
		return 0.05f;
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

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		return null;
	}
}
