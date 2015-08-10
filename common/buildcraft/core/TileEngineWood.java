/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.power.IRedstoneEngine;
import buildcraft.api.power.IRedstoneEngineReceiver;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.engines.TileEngineBase;

public class TileEngineWood extends TileEngineBase implements IRedstoneEngine {
	@Override
	public String getResourcePrefix() {
		return "buildcraftcore:textures/blocks/engineWood";
	}

	@Override
	public ResourceLocation getTrunkTexture(EnergyStage stage) {
		return super.getTrunkTexture(stage == EnergyStage.RED && progress < 0.5 ? EnergyStage.YELLOW : stage);
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
		if (energyLevel < 0.33f) {
			return EnergyStage.BLUE;
		} else if (energyLevel < 0.66f) {
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
				addEnergy(10);
			}
		}
	}

	@Override
	public ConnectOverride overridePipeConnection(IPipeTile.PipeType type, ForgeDirection with) {
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
	public int calculateCurrentOutput() {
		return 10;
	}

	@Override
	public int maxEnergyExtracted() {
		return 10;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return false;
	}

	@Override
	public int getEnergyStored(ForgeDirection side) {
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection side) {
		return 0;
	}

	@Override
	protected void sendPower() {
		TileEntity tile = getTile(orientation);

		if (tile instanceof IRedstoneEngineReceiver && ((IRedstoneEngineReceiver) tile).canConnectRedstoneEngine(orientation.getOpposite())) {
			super.sendPower();
		} else {
			this.energy = 0;
		}
	}
}
