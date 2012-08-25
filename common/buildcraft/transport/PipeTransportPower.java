/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.core.IMachine;
import buildcraft.core.ProxyCore;
import buildcraft.core.Utils;
import buildcraft.transport.network.PacketPowerUpdate;

public class PipeTransportPower extends PipeTransport {
	
	public short[] displayPower = new short[] { 0, 0, 0, 0, 0, 0 };

	public int[] powerQuery = new int[6];
	public int[] nextPowerQuery = new int[6];
	public long currentDate;

	public double[] internalPower = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
	public double[] internalNextPower = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	public double powerResitance = 0.01;

	public PipeTransportPower() {
		for (int i = 0; i < 6; ++i)
			powerQuery[i] = 0;
	}

	SafeTimeTracker tracker = new SafeTimeTracker();

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		return tile instanceof TileGenericPipe || tile instanceof IMachine || tile instanceof IPowerReceptor;
	}

	@Override
	public void updateEntity() {
		if (ProxyCore.proxy.isRemote(worldObj))
			return;

		step();

		TileEntity tiles[] = new TileEntity[6];

		// Extract the nearby connected tiles

		for (int i = 0; i < 6; ++i)
			if (Utils.checkPipesConnections(container.getTile(Orientations.values()[i]), container))
				tiles[i] = container.getTile(Orientations.values()[i]);

		// Send the power to nearby pipes who requested it

		displayPower = new short[] { 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < 6; ++i)
			if (internalPower[i] > 0) {
				double div = 0;

				for (int j = 0; j < 6; ++j)
					if (j != i && powerQuery[j] > 0)
						if (tiles[j] instanceof TileGenericPipe || tiles[j] instanceof IPowerReceptor)
							div += powerQuery[j];

				double totalWatt = internalPower[i];

				for (int j = 0; j < 6; ++j)
					if (j != i && powerQuery[j] > 0) {
						double watts = (totalWatt / div * powerQuery[j]);

						if (tiles[j] instanceof TileGenericPipe) {
							TileGenericPipe nearbyTile = (TileGenericPipe) tiles[j];

							PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;

							nearbyTransport.receiveEnergy(Orientations.values()[j].reverse(), watts);

							displayPower[j] += watts / 2F;
							displayPower[i] += watts / 2F;

							internalPower[i] -= watts;
						} else if (tiles[j] instanceof IPowerReceptor) {
							IPowerReceptor pow = (IPowerReceptor) tiles[j];

							pow.getPowerProvider().receiveEnergy((float) watts, Orientations.values()[j].reverse());

							displayPower[j] += watts / 2F;
							displayPower[i] += watts / 2F;

							internalPower[i] -= watts;
						}
					}
			}

		// Compute the tiles requesting energy that are not pipes

		for (int i = 0; i < 6; ++i)
			if (tiles[i] instanceof IPowerReceptor && !(tiles[i] instanceof TileGenericPipe)) {
				IPowerReceptor receptor = (IPowerReceptor) tiles[i];
				int request = receptor.powerRequest();

				if (request > 0)
					requestEnergy(Orientations.values()[i], request);
			}

		// Sum the amount of energy requested on each side

		int transferQuery[] = { 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < 6; ++i) {
			transferQuery[i] = 0;

			for (int j = 0; j < 6; ++j)
				if (j != i)
					transferQuery[i] += powerQuery[j];
		}

		// Transfer the requested energy to nearby pipes

		for (int i = 0; i < 6; ++i)
			if (transferQuery[i] != 0)
				if (tiles[i] != null) {
					TileEntity entity = tiles[i];

					if (entity instanceof TileGenericPipe) {
						TileGenericPipe nearbyTile = (TileGenericPipe) entity;

						if (nearbyTile.pipe == null)
							continue;
						
						PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;
						nearbyTransport.requestEnergy(Orientations.values()[i].reverse(), transferQuery[i]);
					}
				}
		
		if (!worldObj.isRemote && tracker.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
				PacketPowerUpdate packet = new PacketPowerUpdate(xCoord, yCoord, zCoord);
				packet.displayPower = displayPower;
//				CoreProxy.sendToPlayers(packet.getPacket(), worldObj, xCoord, yCoord, zCoord,
//						DefaultProps.NETWORK_UPDATE_RANGE, mod_BuildCraftCore.instance);
			}

	}

	public void step() {
		if (currentDate != worldObj.getWorldTime()) {
			currentDate = worldObj.getWorldTime();

			powerQuery = nextPowerQuery;
			nextPowerQuery = new int[] { 0, 0, 0, 0, 0, 0 };

			internalPower = internalNextPower;
			internalNextPower = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		}
	}

	public void receiveEnergy(Orientations from, double val) {
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook)
			((IPipeTransportPowerHook) this.container.pipe).receiveEnergy(from, val);
		else {
			if (BuildCraftTransport.usePipeLoss)
				internalNextPower[from.ordinal()] += val * (1 - powerResitance);
			else
				internalNextPower[from.ordinal()] += val;

			if (internalNextPower[from.ordinal()] >= 1000)
				worldObj.createExplosion(null, xCoord, yCoord, zCoord, 2);
		}
	}

	public void requestEnergy(Orientations from, int i) {
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook)
			((IPipeTransportPowerHook) this.container.pipe).requestEnergy(from, i);
		else {
			step();
			nextPowerQuery[from.ordinal()] += i;
		}
	}

	@Override
	public void initialize() {
		currentDate = worldObj.getWorldTime();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			powerQuery[i] = nbttagcompound.getInteger("powerQuery[" + i + "]");
			nextPowerQuery[i] = nbttagcompound.getInteger("nextPowerQuery[" + i + "]");
			internalPower[i] = nbttagcompound.getDouble("internalPower[" + i + "]");
			internalNextPower[i] = nbttagcompound.getDouble("internalNextPower[" + i + "]");
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			nbttagcompound.setInteger("powerQuery[" + i + "]", powerQuery[i]);
			nbttagcompound.setInteger("nextPowerQuery[" + i + "]", nextPowerQuery[i]);
			nbttagcompound.setDouble("internalPower[" + i + "]", internalPower[i]);
			nbttagcompound.setDouble("internalNextPower[" + i + "]", internalNextPower[i]);
		}
	}

	public boolean isTriggerActive(ITrigger trigger) {
		return false;
	}

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportPower;
	}

	/**
	 * Client-side handler for receiving power updates from the server;
	 * @param packetPower
	 */
	public void handlePowerPacket(PacketPowerUpdate packetPower) {
		displayPower = packetPower.displayPower;
	}

}
