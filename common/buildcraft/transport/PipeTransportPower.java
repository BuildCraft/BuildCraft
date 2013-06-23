/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.core.DefaultProps;
import buildcraft.core.IMachine;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PipeTransportPower extends PipeTransport {

	private static final short MAX_DISPLAY = 100;
	private static final int DISPLAY_SMOOTHING = 10;
	private static final int OVERLOAD_TICKS = 60;
	public static final Map<Class<? extends Pipe>, Integer> powerCapacities = new HashMap<Class<? extends Pipe>, Integer>();

	static {
		powerCapacities.put(PipePowerCobblestone.class, 8);
		powerCapacities.put(PipePowerStone.class, 16);
		powerCapacities.put(PipePowerWood.class, 32);
		powerCapacities.put(PipePowerQuartz.class, 64);
		powerCapacities.put(PipePowerGold.class, 256);
		powerCapacities.put(PipePowerDiamond.class, 1024);
	}
	private boolean needsInit = true;
	private TileEntity[] tiles = new TileEntity[6];
	public double[] displayPower = new double[6];
	public double[] prevDisplayPower = new double[6];
	public short[] clientDisplayPower = new short[6];
	public int overload;
	public int[] powerQuery = new int[6];
	public int[] nextPowerQuery = new int[6];
	public long currentDate;
	public double[] internalPower = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	public double[] internalNextPower = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	public int maxPower = 8;

	public PipeTransportPower() {
		for (int i = 0; i < 6; ++i) {
			powerQuery[i] = 0;
		}
	}
	SafeTimeTracker tracker = new SafeTimeTracker();

	public void initFromPipe(Class<? extends Pipe> pipeClass) {
		maxPower = powerCapacities.get(pipeClass);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportPower))
				return false;
		}

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
				internalPower[i] = 0;
				internalNextPower[i] = 0;
				displayPower[i] = 0;
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

		System.arraycopy(displayPower, 0, prevDisplayPower, 0, 6);
		Arrays.fill(displayPower, 0.0);

		for (int i = 0; i < 6; ++i) {
			if (internalPower[i] > 0) {
				double totalPowerQuery = 0;

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0)
						if (tiles[j] instanceof TileGenericPipe || tiles[j] instanceof IPowerReceptor) {
							totalPowerQuery += powerQuery[j];
						}
				}

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0) {
						double watts = 0.0;

						if (tiles[j] instanceof TileGenericPipe) {
							watts = (internalPower[i] / totalPowerQuery * powerQuery[j]);
							TileGenericPipe nearbyTile = (TileGenericPipe) tiles[j];

							PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;

							watts = nearbyTransport.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[j].getOpposite(), watts);
							internalPower[i] -= watts;
						} else if (tiles[j] instanceof IPowerReceptor) {
							watts = (internalPower[i] / totalPowerQuery * powerQuery[j]);
							IPowerReceptor pow = (IPowerReceptor) tiles[j];

							IPowerProvider prov = pow.getPowerProvider();

							if (prov != null) {
								prov.receiveEnergy((float) watts, ForgeDirection.VALID_DIRECTIONS[j].getOpposite());
								internalPower[i] -= watts;
							}
						}

						displayPower[j] += watts;
						displayPower[i] += watts;
					}
				}
			}
		}

		double highestPower = 0;
		for (int i = 0; i < 6; i++) {
			displayPower[i] = (prevDisplayPower[i] * (DISPLAY_SMOOTHING - 1.0) + displayPower[i]) / DISPLAY_SMOOTHING;
			if (displayPower[i] > highestPower) {
				highestPower = displayPower[i];
			}
		}

		overload += highestPower > maxPower * 0.95 ? 1 : -1;
		if (overload < 0) {
			overload = 0;
		}
		if (overload > OVERLOAD_TICKS) {
			overload = OVERLOAD_TICKS;
		}

		// Compute the tiles requesting energy that are not pipes

		for (int i = 0; i < 6; ++i) {
			if (tiles[i] instanceof IPowerReceptor && !(tiles[i] instanceof TileGenericPipe)) {
				IPowerReceptor receptor = (IPowerReceptor) tiles[i];
				int request = receptor.powerRequest(ForgeDirection.VALID_DIRECTIONS[i].getOpposite());

				if (request > 0) {
					requestEnergy(ForgeDirection.VALID_DIRECTIONS[i], request);
				}
			}
		}

		// Sum the amount of energy requested on each side

		int[] transferQuery = new int[6];

		for (int i = 0; i < 6; ++i) {
			transferQuery[i] = 0;

			for (int j = 0; j < 6; ++j) {
				if (j != i) {
					transferQuery[i] += powerQuery[j];
				}
			}

			transferQuery[i] = Math.min(transferQuery[i], maxPower);
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

			double displayFactor = MAX_DISPLAY / 1024.0;
			for (int i = 0; i < clientDisplayPower.length; i++) {
				clientDisplayPower[i] = (short) (displayPower[i] * displayFactor + .9999);
			}

			packet.displayPower = clientDisplayPower;
			packet.overload = overload >= OVERLOAD_TICKS;
			CoreProxy.proxy.sendToPlayers(packet.getPacket(), worldObj, xCoord, yCoord, zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
		}

	}

	public void step() {
		if (currentDate != worldObj.getWorldTime()) {
			currentDate = worldObj.getWorldTime();

			powerQuery = nextPowerQuery;
			nextPowerQuery = new int[6];

			double[] next = internalPower;
			internalPower = internalNextPower;
			internalNextPower = next;
			for (int i = 0; i < powerQuery.length; i++) {
				int sum = 0;
				for (int j = 0; j < powerQuery.length; j++) {
					if (i != j) {
						sum += powerQuery[j];
					}
				}
				if (sum == 0 && internalNextPower[i] > 0) {
					internalNextPower[i] -= 1;
					if (internalNextPower[i] < 0) {
						internalNextPower[i] = 0;
					}
				}
			}
		}
	}

	public double receiveEnergy(ForgeDirection from, double val) {
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			return ((IPipeTransportPowerHook) this.container.pipe).receiveEnergy(from, val);
		} else {
			internalNextPower[from.ordinal()] += val;

			if (internalNextPower[from.ordinal()] > maxPower) {
				val = internalNextPower[from.ordinal()] - maxPower;
				internalNextPower[from.ordinal()] = maxPower;
			}
		}
		return val;
	}

	public void requestEnergy(ForgeDirection from, int amount) {
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			((IPipeTransportPowerHook) this.container.pipe).requestEnergy(from, amount);
		} else {
			nextPowerQuery[from.ordinal()] += amount;
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

	/**
	 * Client-side handler for receiving power updates from the server;
	 *
	 * @param packetPower
	 */
	public void handlePowerPacket(PacketPowerUpdate packetPower) {
		clientDisplayPower = packetPower.displayPower;
		overload = packetPower.overload ? OVERLOAD_TICKS : 0;
	}
}
