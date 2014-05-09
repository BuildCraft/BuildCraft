/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.TileBuffer;
import buildcraft.core.TileBuildCraft;

public class TileEnergyReceiver extends TileBuildCraft implements IPipeConnection, IPowerEmitter {
	public static LinkedList<TileEnergyReceiver> knownReceivers = new LinkedList<TileEnergyReceiver>();

	public float energyStored = 0;

	private TileBuffer[] tileCache;

	public TileEnergyReceiver () {
		knownReceivers.add(this);
	}

	@Override
	public void invalidate() {
		knownReceivers.remove(this);
	}

	public TileBuffer getTileBuffer(ForgeDirection side) {
		if (tileCache == null) {
			tileCache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		}

		return tileCache[side.ordinal()];
	}

	public boolean isPoweredTile(TileEntity tile, ForgeDirection side) {
		if (tile instanceof IPowerReceptor) {
			return ((IPowerReceptor) tile).getPowerReceiver(side.getOpposite()) != null;
		} else if (MjAPI.getMjBattery(tile) != null) {
			return true;
		}

		return false;
	}

	private void sendPower() {
		for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = getTileBuffer(s).getTile();

			if (tile instanceof IPowerReceptor) {
				PowerReceiver receptor = ((IPowerReceptor) tile)
						.getPowerReceiver(s.getOpposite());

				if (receptor != null) {
					receptor.receiveEnergy(PowerHandler.Type.ENGINE, energyStored,
							s.getOpposite());

					energyStored = 0;
				}
			} else if (tile != null) {
				IBatteryObject battery = MjAPI.getMjBattery(tile);

				if (battery != null) {
					battery.addEnergy(energyStored);
					energyStored = 0;
				}
			}
		}
	}

	@Override
	public ConnectOverride overridePipeConnection(PipeType type,
			ForgeDirection with) {
		return type == PipeType.POWER ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT;
	}

	@Override
	public void updateEntity () {
		sendPower ();
	}

	@Override
	public boolean canEmitPowerFrom(ForgeDirection side) {
		return true;
	}

}