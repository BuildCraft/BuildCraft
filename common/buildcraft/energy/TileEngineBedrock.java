/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.proxy.CoreProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

public class TileEngineBedrock extends TileEngine {

	public static final float OUTPUT = 1F;

	public TileEngineBedrock() {
		super(0);
	}

	@Override
	public ResourceLocation getTextureFile() {
		return BEDROCK_TEXTURE;
	}

	@Override
	public float explosionRange() {
		return 0;
	}

	@Override
	public float maxEnergyReceived() {
		return 1024;
	}

	@Override
	protected EnergyStage computeEnergyStage() {
		return isRedstonePowered ? EnergyStage.YELLOW : EnergyStage.BLUE;
	}

	@Override
	public float getPistonSpeed() {
		return 0.1F;
	}

	@Override
	public void engineUpdate() {
		super.engineUpdate();

		if (isRedstonePowered)
			if(this.energy < 9000) addEnergy(9000);
	}

	@Override
	public boolean isBurning() {
		return isRedstonePowered;
	}

	@Override
	public int getScaledBurnTime(int i) {
		return 0;
	}

	@Override
	public float getMaxEnergy() {
		return 9000;
	}

	@Override
	public float getCurrentOutput() {
		return OUTPUT;
	}

	@Override
	public float maxEnergyExtracted() {
		return 1024;
	}

	@Override
	public void checkRedstonePower() {
		isRedstonePowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		energyStage = computeEnergyStage();
	}
}
