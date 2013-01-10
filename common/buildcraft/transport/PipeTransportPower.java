/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.core.DefaultProps;
import buildcraft.core.IMachine;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.network.PacketPowerUpdate;

public class PipeTransportPower extends PipeTransport {

	private static final int MAX_POWER_INTERNAL = 10000;
	private static final int OVERLOAD_LIMIT = 7500;
	private static final short MAX_DISPLAY = 100;
	private static final float DISPLAY_POWER_FACTOR = 0.1f;

	private boolean needsInit = true;

	private TileEntity[] tiles = new TileEntity[6];

	public short[] displayPower = new short[] { 0, 0, 0, 0, 0, 0 };
	public boolean overload;

	public int[] powerQuery = new int[6];
	public int[] nextPowerQuery = new int[6];
	public long currentDate;

	public double[] internalPower = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
	public double[] internalNextPower = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	public double powerResistance = 0.05;

	public PipeTransportPower() {
		for (int i = 0; i < 6; ++i) {
			powerQuery[i] = 0;
		}
	}

	SafeTimeTracker tracker = new SafeTimeTracker();

	@Override
	public boolean isPipeConnected(TileEntity tile, ForgeDirection side) {
		return tile instanceof TileGenericPipe || tile instanceof IMachine || tile instanceof IPowerReceptor;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);
		updateTiles();
	}

	private void updateTiles() {
		for (int i = 0; i < 6; ++i) {
			TileEntity tile = container.getTile(ForgeDirection.VALID_DIRECTIONS[i]);
			if (Utils.checkPipesConnections(tile, container)) {
				tiles[i] = tile;
			} else {
				tiles[i] = null;
			}
		}
	}

	private void init() {
		if (needsInit) {
			needsInit = false;
			updateTiles();
		}
	}

	@Override
	public void updateEntity() {
		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		step();

		init();

		// Send the power to nearby pipes who requested it

		Arrays.fill(displayPower, (short) 0);

		for (int i = 0; i < 6; ++i) {
			if (internalPower[i] > 0) {
				double div = 0;

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0)
						if (tiles[j] instanceof TileGenericPipe || tiles[j] instanceof IPowerReceptor) {
							div += powerQuery[j];
						}
				}

				double totalWatt = internalPower[i];

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0) {
						double watts = (totalWatt / div * powerQuery[j]);

						if (tiles[j] instanceof TileGenericPipe) {
							TileGenericPipe nearbyTile = (TileGenericPipe) tiles[j];

							PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;

							nearbyTransport.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[j].getOpposite(), watts);

							displayPower[j] += (short) Math.ceil(watts * DISPLAY_POWER_FACTOR);
							displayPower[i] += (short) Math.ceil(watts * DISPLAY_POWER_FACTOR);

							internalPower[i] -= watts;
						} else if (tiles[j] instanceof IPowerReceptor) {
							IPowerReceptor pow = (IPowerReceptor) tiles[j];

							IPowerProvider prov = pow.getPowerProvider();

							if (prov != null) {
								prov.receiveEnergy((float) watts, ForgeDirection.VALID_DIRECTIONS[j].getOpposite());

								displayPower[j] += (short) Math.ceil(watts * DISPLAY_POWER_FACTOR);
								displayPower[i] += (short) Math.ceil(watts * DISPLAY_POWER_FACTOR);

								internalPower[i] -= watts;
							}
						}
					}
				}
			}
		}

		double highestPower = 0;
		for (int i = 0; i < 6; i++) {
			if (internalPower[i] > highestPower) {
				highestPower = internalPower[i];
			}
			displayPower[i] = (short) Math.max(displayPower[i], Math.ceil(internalPower[i] * DISPLAY_POWER_FACTOR));
			displayPower[i] = (short) Math.min(displayPower[i], MAX_DISPLAY);
		}
		overload = highestPower > OVERLOAD_LIMIT;

		// Compute the tiles requesting energy that are not pipes

		for (int i = 0; i < 6; ++i) {
			if (tiles[i] instanceof IPowerReceptor && !(tiles[i] instanceof TileGenericPipe)) {
				IPowerReceptor receptor = (IPowerReceptor) tiles[i];
				int request = receptor.powerRequest();

				if (request > 0) {
					requestEnergy(ForgeDirection.VALID_DIRECTIONS[i], request);
				}
			}
		}

		// Sum the amount of energy requested on each side

		int transferQuery[] = { 0, 0, 0, 0, 0, 0 };

		for (int i = 0; i < 6; ++i) {
			transferQuery[i] = 0;

			for (int j = 0; j < 6; ++j) {
				if (j != i) {
					transferQuery[i] += powerQuery[j];
				}
			}
		}

		// Transfer the requested energy to nearby pipes

		for (int i = 0; i < 6; ++i) {
			if (transferQuery[i] != 0) {
				if (tiles[i] != null) {
					TileEntity entity = tiles[i];

					if (entity instanceof TileGenericPipe) {
						TileGenericPipe nearbyTile = (TileGenericPipe) entity;

						if (nearbyTile.pipe == null) {
							continue;
						}

						PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;
						nearbyTransport.requestEnergy(ForgeDirection.VALID_DIRECTIONS[i].getOpposite(), transferQuery[i]);
					}
				}
			}
		}

		if (tracker.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
			PacketPowerUpdate packet = new PacketPowerUpdate(xCoord, yCoord, zCoord);
			packet.displayPower = displayPower;
			packet.overload = overload;
			CoreProxy.proxy.sendToPlayers(packet.getPacket(), worldObj, xCoord, yCoord, zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
		}

	}

	public void step() {
		if (currentDate != worldObj.getWorldTime()) {
			currentDate = worldObj.getWorldTime();

			powerQuery = nextPowerQuery;
			nextPowerQuery = new int[] { 0, 0, 0, 0, 0, 0 };

			double[] next = Arrays.copyOf(internalPower, 6);
			internalPower = internalNextPower;
			internalNextPower = next;
			for (int i = 0; i < nextPowerQuery.length; i++) {
				if (powerQuery[i] == 0.0d && internalNextPower[i] > 0) {
					internalNextPower[i] -= 1;
				}
			}
		}
	}

	public void receiveEnergy(ForgeDirection from, double val) {
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			((IPipeTransportPowerHook) this.container.pipe).receiveEnergy(from, val);
		} else {
			if (BuildCraftTransport.usePipeLoss) {
				internalNextPower[from.ordinal()] += val * (1 - powerResistance);
			} else {
				internalNextPower[from.ordinal()] += val;
			}

			if (internalNextPower[from.ordinal()] >= MAX_POWER_INTERNAL) {
				worldObj.createExplosion(null, xCoord, yCoord, zCoord, 3, false);
				worldObj.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
			}
		}
	}

	public void requestEnergy(ForgeDirection from, int i) {
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			((IPipeTransportPowerHook) this.container.pipe).requestEnergy(from, i);
		} else {
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
	 * 
	 * @param packetPower
	 */
	public void handlePowerPacket(PacketPowerUpdate packetPower) {
		displayPower = packetPower.displayPower;
		overload = packetPower.overload;
	}

}
