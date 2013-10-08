/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.api.power.PowerHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.proxy.CoreProxy;

public class TileEngineWood extends TileEngine {

	public static final float OUTPUT = 0.05F;

	@Override
	public ResourceLocation getTextureFile() {
		return WOOD_TEXTURE;
	}

	@Override
	public float explosionRange() {
		return 1;
	}

	@Override
	public float minEnergyReceived() {
		return 0;
	}

	@Override
	public float maxEnergyReceived() {
		return 50;
	}

	@Override
	protected EnergyStage computeEnergyStage() {
		float energyLevel = getEnergyLevel();
		if (energyLevel < 0.25f)
			return EnergyStage.BLUE;
		else if (energyLevel < 0.5f)
			return EnergyStage.GREEN;
		else if (energyLevel < 0.75f)
			return EnergyStage.YELLOW;
		else
			return EnergyStage.RED;
	}

	@Override
	public float getPistonSpeed() {
		if (CoreProxy.proxy.isSimulating(worldObj))
			return Math.max(0.08f * getHeatLevel(), 0.01f);
		switch (getEnergyStage()) {
			case GREEN:
				return 0.02F;
			case YELLOW:
				return 0.04F;
			case RED:
				return 0.08F;
			default:
				return 0.01F;
		}
	}

	@Override
	public void engineUpdate() {
		super.engineUpdate();

		if (isRedstonePowered)
			if (worldObj.getTotalWorldTime() % 16 == 0)
				addEnergy(1);
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with) {
		return ConnectOverride.DISCONNECT;
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
		return 100;
	}

	@Override
	public float getCurrentOutput() {
		return OUTPUT;
	}

	@Override
	public float maxEnergyExtracted() {
		return 1 + PowerHandler.PerditionCalculator.MIN_POWERLOSS;
	}
}
