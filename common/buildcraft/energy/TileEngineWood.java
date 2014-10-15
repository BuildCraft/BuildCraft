/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.power.PowerHandler;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.transport.TileGenericPipe;

public class TileEngineWood extends TileEngine {

	public static final int OUTPUT = 1;

	@Override
	public ResourceLocation getBaseTexture() {
		return BASE_TEXTURES[0];
	}

	@Override
	public ResourceLocation getChamberTexture() {
		return CHAMBER_TEXTURES[0];
	}

	@Override
	public float explosionRange() {
		return 1;
	}

	@Override
	public int minEnergyReceived() {
		return 0;
	}

	@Override
	public int maxEnergyReceived() {
		return 500;
	}

	@Override
	protected EnergyStage computeEnergyStage() {
		double energyLevel = getEnergyLevel();
		if (energyLevel < 0.25f) {
			return EnergyStage.BLUE;
		} else if (energyLevel < 0.5f) {
			return EnergyStage.GREEN;
		} else if (energyLevel < 0.75f) {
			return EnergyStage.YELLOW;
		} else {
			return EnergyStage.RED;
		}
	}

	@Override
	public float getPistonSpeed() {
		if (!worldObj.isRemote) {
			return Math.max(0.08f * getHeatLevel(), 0.01f);
		}

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

		if (isRedstonePowered) {
			if (worldObj.getTotalWorldTime() % 16 == 0) {
				addEnergy(1);
			}
		}
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
	public int getMaxEnergy() {
		return 1000;
	}

	@Override
	public int getCurrentOutput() {
		return OUTPUT;
	}

	@Override
	public int maxEnergyExtracted() {
		return 1;
	}

	// TODO: HACK
	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return false;
	}

	@Override
	protected void sendPower() {
		TileEntity tile = getTileBuffer(orientation).getTile();
		if (tile instanceof TileGenericPipe && ((TileGenericPipe) tile).getPipeType() != PipeType.POWER) {
			super.sendPower();
		} else { // pretend we're sending out our powers
			this.energy = 0;
		}
	}
}
