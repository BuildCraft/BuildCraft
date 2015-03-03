/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.pipes.PipePowerWood;

public class TriggerEnergy extends BCStatement implements ITriggerInternal {
	private boolean high;

	public TriggerEnergy(String name, boolean high) {
		super(name);

		this.high = high;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.machine.energyStored" + (high ? "High" : "Low"));
	}

	private boolean isTriggeredEnergyHandler(IEnergyConnection connection, ForgeDirection side) {
		int energyStored, energyMaxStored;

		if (connection instanceof IEnergyHandler) {
			energyStored = ((IEnergyHandler) connection).getEnergyStored(side);
			energyMaxStored = ((IEnergyHandler) connection).getMaxEnergyStored(side);
		} else if (connection instanceof IEnergyProvider) {
			energyStored = ((IEnergyProvider) connection).getEnergyStored(side);
			energyMaxStored = ((IEnergyProvider) connection).getMaxEnergyStored(side);
		} else if (connection instanceof IEnergyReceiver) {
			energyStored = ((IEnergyReceiver) connection).getEnergyStored(side);
			energyMaxStored = ((IEnergyReceiver) connection).getMaxEnergyStored(side);
		} else {
			return false;
		}

		if (energyMaxStored > 0) {
			if (high) {
				return (energyStored / energyMaxStored) > 0.95;
			} else {
				return (energyStored / energyMaxStored) < 0.05;
			}
		}
		return false;
	}

	protected boolean isTriggered(Object tile, ForgeDirection side) {
		if (tile instanceof IEnergyHandler || tile instanceof IEnergyProvider || tile instanceof IEnergyReceiver) {
			if (((IEnergyConnection) tile).canConnectEnergy(side.getOpposite())) {
				return isTriggeredEnergyHandler((IEnergyConnection) tile, side.getOpposite());
			}
		}

		return false;
	}

	public static boolean isTriggeringPipe(TileEntity tile) {
		if (tile instanceof IPipeTile) {
			IPipeTile pipeTile = (IPipeTile) tile;
			if (pipeTile.getPipeType() == IPipeTile.PipeType.POWER && pipeTile.getPipe() instanceof IEnergyHandler) {
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/trigger_machine_energy_" + (high ? "high" : "low"));
	}

	@Override
	public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
		// Internal check
		if (isTriggeringPipe(source.getTile())) {
			return isTriggered(((IPipeTile) source.getTile()).getPipe(), ForgeDirection.UNKNOWN);
		}

		TileEntity parent = source.getTile();

		if (parent instanceof IPipeTile) {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = ((IPipeTile) parent).getNeighborTile(side);
				if (tile != null && isTriggered(tile, side)) {
					return true;
				}
			}
		} else {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = parent.getWorldObj().getTileEntity(
						source.getTile().xCoord + side.offsetX,
						source.getTile().yCoord + side.offsetY,
						source.getTile().zCoord + side.offsetZ
				);
				if (tile != null && isTriggered(tile, side)) {
					return true;
				}
			}
		}
		return false;
	}
}
