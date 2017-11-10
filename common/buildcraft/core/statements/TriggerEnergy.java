/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
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
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.utils.StringUtils;

public class TriggerEnergy extends BCStatement implements ITriggerInternal {
	public static class Neighbor {
		public TileEntity tile;
		public ForgeDirection side;

		public Neighbor(TileEntity tile, ForgeDirection side) {
			this.tile = tile;
			this.side = side;
		}
	}

	private final boolean high;

	public TriggerEnergy(boolean high) {
		super("buildcraft:energyStored" + (high ? "high" : "low"));

		this.high = high;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.machine.energyStored." + (high ? "high" : "low"));
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
			float level = (float) energyStored / (float) energyMaxStored;
			if (high) {
				return level > 0.95F;
			} else {
				return level < 0.05F;
			}
		}
		return false;
	}

	protected static boolean isTriggered(Object tile, ForgeDirection side) {
		return (tile instanceof IEnergyHandler || tile instanceof IEnergyProvider || tile instanceof IEnergyReceiver)
				&& (((IEnergyConnection) tile).canConnectEnergy(side.getOpposite()));
	}

	protected boolean isActive(Object tile, ForgeDirection side) {
		if (isTriggered(tile, side)) {
			return isTriggeredEnergyHandler((IEnergyConnection) tile, side.getOpposite());
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
		icon = iconRegister.registerIcon("buildcraftcore:triggers/trigger_energy_storage_" + (high ? "high" : "low"));
	}

	@Override
	public boolean isTriggerActive(IStatementContainer source, IStatementParameter[] parameters) {
		// Internal check
		if (isTriggeringPipe(source.getTile())) {
			return isActive(((IPipeTile) source.getTile()).getPipe(), ForgeDirection.UNKNOWN);
		}

		Neighbor triggeringNeighbor = getTriggeringNeighbor(source.getTile());
		if (triggeringNeighbor != null) {
			return isActive(triggeringNeighbor.tile, triggeringNeighbor.side);
		}
		return false;
	}

	public static Neighbor getTriggeringNeighbor(TileEntity parent) {
		if (parent instanceof IPipeTile) {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = ((IPipeTile) parent).getNeighborTile(side);
				if (tile != null && isTriggered(tile, side)) {
					return new Neighbor(tile, side);
				}
			}
		} else {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = parent.getWorldObj().getTileEntity(
						parent.xCoord + side.offsetX,
						parent.yCoord + side.offsetY,
						parent.zCoord + side.offsetZ
				);
				if (tile != null && isTriggered(tile, side)) {
					return new Neighbor(tile, side);
				}
			}
		}
		return null;
	}
}
