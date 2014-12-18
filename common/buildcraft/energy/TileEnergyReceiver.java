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

import cofh.api.energy.IEnergyHandler;

import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeContainer.PipeType;
import buildcraft.core.TileBuffer;
import buildcraft.core.TileBuildCraft;

public class TileEnergyReceiver extends TileBuildCraft implements IPipeConnection {
	public static LinkedList<TileEnergyReceiver> knownReceivers = new LinkedList<TileEnergyReceiver>();

	public int energyStored = 0;

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
		if (tile instanceof IEnergyHandler) {
			return ((IEnergyHandler) tile).canConnectEnergy(side.getOpposite());
		}

		return false;
	}

	private void sendPower() {
		for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = getTileBuffer(s).getTile();

			if (tile instanceof IEnergyHandler) {
				int energyUsed = ((IEnergyHandler) tile).receiveEnergy(s.getOpposite(), energyStored, false);
				energyStored -= energyUsed;
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
		sendPower();
	}
}