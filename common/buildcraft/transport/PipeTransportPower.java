/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.PipePowerCobblestone;
import buildcraft.transport.pipes.PipePowerDiamond;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerHeat;
import buildcraft.transport.pipes.PipePowerIron;
import buildcraft.transport.pipes.PipePowerQuartz;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerWood;

public class PipeTransportPower extends PipeTransport {

	public static final Map<Class<? extends Pipe>, Integer> powerCapacities = new HashMap<Class<? extends Pipe>, Integer>();

	private static final short MAX_DISPLAY = 100;
	private static final int DISPLAY_SMOOTHING = 10;
	private static final int OVERLOAD_TICKS = 60;

	public float[] displayPower = new float[6];
	public short[] clientDisplayPower = new short[6];
	public int overload;
	public double[] nextPowerQuery = new double[6];
	public double[] internalNextPower = new double[6];
	public double maxPower = 8;
	public float[] movementStage = new float[] {0, 0, 0};

	private boolean needsInit = true;
	private TileEntity[] tiles = new TileEntity[6];

	private float[] prevDisplayPower = new float[6];

	private double[] powerQuery = new double[6];

	private long currentDate;
	private double[] internalPower = new double[6];

	private double highestPower;
	private SafeTimeTracker tracker = new SafeTimeTracker(2 * BuildCraftCore.updateFactor);

	public PipeTransportPower() {
		for (int i = 0; i < 6; ++i) {
			powerQuery[i] = 0;
		}

		for (int i = 0; i < 3; ++i) {
			movementStage[i] = (float) Math.random();
		}
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.POWER;
	}

	public void initFromPipe(Class<? extends Pipe> pipeClass) {
		maxPower = powerCapacities.get(pipeClass);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportPower)) {
				return false;
			}
			return true;
		}

		if (tile instanceof IPowerReceptor) {
			IPowerReceptor receptor = (IPowerReceptor) tile;
			PowerReceiver receiver = receptor.getPowerReceiver(side.getOpposite());
			if (receiver != null && receiver.getType().canReceiveFromPipes()) {
				return true;
			}
		}

		if (container.pipe instanceof PipePowerWood && tile instanceof IPowerEmitter) {
			IPowerEmitter emitter = (IPowerEmitter) tile;
			if (emitter.canEmitPowerFrom(side.getOpposite())) {
				return true;
			}
		}

		if (MjAPI.getMjBattery(tile, MjAPI.DEFAULT_POWER_FRAMEWORK, side.getOpposite()) != null) {
			return true;
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);
		updateTiles();
	}

	private void updateTiles() {
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = container.getTile(side);
			if (container.isPipeConnected(side)) {
				tiles[side.ordinal()] = tile;
			} else {
				tiles[side.ordinal()] = null;
				internalPower[side.ordinal()] = 0;
				internalNextPower[side.ordinal()] = 0;
				displayPower[side.ordinal()] = 0;
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
		if (container.getWorldObj().isRemote) {
			// updating movement stage. We're only carrying the movement on half
			// the things. This is purely for animation purpose.

			for (int i = 0; i < 6; i += 2) {
				movementStage [i / 2] = (movementStage [i / 2] + 0.01F) % 1.0F;
			}

			return;
		}

		step();

		init();

		// Send the power to nearby pipes who requested it

		System.arraycopy(displayPower, 0, prevDisplayPower, 0, 6);
		Arrays.fill(displayPower, 0.0F);

		// STEP 1 - computes the total amount of power contained and total
		// amount of power queried

		double totalPowerContained = 0;

		for (int in = 0; in < 6; ++in) {
			totalPowerContained += internalPower[in];
		}

		double totalPowerQuery = 0;

		for (int out = 0; out < 6; ++out) {
			if (internalPower[out] == 0) {
				totalPowerQuery += powerQuery[out];
			}
		}

		// STEP 2 - sends the power to all directions and computes the actual
		// amount of power that was consumed

		double totalPowerConsumed = 0;

		if (totalPowerContained > 0) {
			for (int out = 0; out < 6; ++out) {
				if (powerQuery[out] > 0 && internalPower[out] == 0) {
					double powerConsumed = powerQuery[out] / totalPowerQuery * totalPowerContained;

					if (tiles[out] instanceof TileGenericPipe) {
						// Transmit power to the nearby pipe

						TileGenericPipe nearbyTile = (TileGenericPipe) tiles[out];
						PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;
						powerConsumed = nearbyTransport.receiveEnergy(
								ForgeDirection.VALID_DIRECTIONS[out].getOpposite(),
								powerConsumed);
					} else {
						IBatteryObject battery = MjAPI.getMjBattery(tiles[out], MjAPI.DEFAULT_POWER_FRAMEWORK,
								ForgeDirection.VALID_DIRECTIONS[out].getOpposite());

						if (battery != null) {
							// Transmit power to the simplified power framework
							powerConsumed = battery.addEnergy(powerConsumed);
						} else {
							PowerReceiver prov = getReceiverOnSide(ForgeDirection.VALID_DIRECTIONS[out]);

							if (prov != null) {
								// Transmit power to the legacy power framework

								powerConsumed = prov.receiveEnergy(Type.PIPE, powerConsumed,
										ForgeDirection.VALID_DIRECTIONS[out].getOpposite());
							}
						}
					}

					displayPower[out] += powerConsumed;
					totalPowerConsumed += powerConsumed;
				}
			}
		}

		// STEP 3 - assume equal repartition of all consumed locations and
		// compute display for each source of power

		if (totalPowerConsumed > 0) {
			for (int in = 0; in < 6; ++in) {
				double powerConsumed = internalPower[in] / totalPowerContained * totalPowerConsumed;
				displayPower[in] += powerConsumed;
			}
		}

		// NEXT STEPS... other things to do...

		highestPower = 0;
		for (int i = 0; i < 6; i++) {
			displayPower[i] = (prevDisplayPower[i] * (DISPLAY_SMOOTHING - 1.0F) + displayPower[i]) / DISPLAY_SMOOTHING;

			if (displayPower[i] > highestPower) {
				highestPower = displayPower[i];
			}

			if (displayPower[i] < 0.01) {
				displayPower[i] = 0;
			}
		}

		overload += highestPower > maxPower * 0.95 ? 1 : -1;
		if (overload < 0) {
			overload = 0;
		}
		if (overload > OVERLOAD_TICKS) {
			overload = OVERLOAD_TICKS;
		}

		// Compute the tiles requesting energy that are not power pipes

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = tiles [dir.ordinal()];

			if (!(tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportPower)) {
				PowerReceiver prov = getReceiverOnSide(dir);
				if (prov != null) {
					double request = prov.powerRequest();

					if (request > 0) {
						requestEnergy(dir, request);
					}
				}

				if (tile != null) {
					IBatteryObject battery = MjAPI.getMjBattery(tile, MjAPI.DEFAULT_POWER_FRAMEWORK, dir.getOpposite());

					if (battery != null) {
						requestEnergy(dir, battery.getEnergyRequested());
					}
				}
			}
		}

		// Sum the amount of energy requested on each side

		double[] transferQuery = new double[6];

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

		if (tracker.markTimeIfDelay(container.getWorldObj())) {
			PacketPowerUpdate packet = new PacketPowerUpdate(container.xCoord, container.yCoord, container.zCoord);

			double displayFactor = MAX_DISPLAY / 1024.0;
			for (int i = 0; i < clientDisplayPower.length; i++) {
				clientDisplayPower[i] = (short) (Math.ceil(displayPower[i] * displayFactor));
			}

			packet.displayPower = clientDisplayPower;
			packet.overload = isOverloaded();
			BuildCraftTransport.instance.sendToPlayers(packet, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
		}
	}

	private PowerReceiver getReceiverOnSide(ForgeDirection side) {
		TileEntity tile = tiles[side.ordinal()];
		if (!(tile instanceof IPowerReceptor)) {
			return null;
		}
		IPowerReceptor receptor = (IPowerReceptor) tile;
		PowerReceiver receiver = receptor.getPowerReceiver(side.getOpposite());
		if (receiver == null) {
			return null;
		}
		if (!receiver.getType().canReceiveFromPipes()) {
			return null;
		}
		return receiver;
	}

	public boolean isOverloaded() {
		return overload >= OVERLOAD_TICKS;
	}

	private void step() {
		if (container != null && container.getWorldObj() != null
				&& currentDate != container.getWorldObj().getTotalWorldTime()) {
			currentDate = container.getWorldObj().getTotalWorldTime();

			powerQuery = nextPowerQuery;
			nextPowerQuery = new double[6];

			internalPower = internalNextPower;
			internalNextPower = new double[6];

			for (int i = 0; i < internalNextPower.length; ++i) {
				internalNextPower[i] = 0;
				nextPowerQuery[i] = 0;
			}
		}
	}

	/**
	 * Do NOT ever call this from outside Buildcraft. It is NOT part of the API.
	 * All power input MUST go through designated input pipes, such as Wooden
	 * Power Pipes or a subclass thereof.
	 */
	public double receiveEnergy(ForgeDirection from, double valI) {
		double val = valI;
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			double ret = ((IPipeTransportPowerHook) this.container.pipe).receiveEnergy(from, val);
			if (ret >= 0) {
				return ret;
			}
		}
		int side = from.ordinal();
		if (internalNextPower[side] > maxPower) {
			return 0;
		}

		internalNextPower[side] += val;

		if (internalNextPower[side] > maxPower) {
			val -= internalNextPower[side] - maxPower;
			internalNextPower[side] = maxPower;
			if (val < 0) {
				val = 0;
			}
		}

		return val;
	}

	public void requestEnergy(ForgeDirection from, double amount) {
		step();

		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			nextPowerQuery[from.ordinal()] += ((IPipeTransportPowerHook) this.container.pipe).requestEnergy(from, amount);
		} else {
			nextPowerQuery[from.ordinal()] += amount;
		}
	}

	@Override
	public void initialize() {
		currentDate = container.getWorldObj().getTotalWorldTime();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			powerQuery[i] = nbttagcompound.getDouble("powerQuery[" + i + "]");
			nextPowerQuery[i] = nbttagcompound.getDouble("nextPowerQuery[" + i + "]");
			internalPower[i] = (float) nbttagcompound.getDouble("internalPower[" + i + "]");
			internalNextPower[i] = (float) nbttagcompound.getDouble("internalNextPower[" + i + "]");
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			nbttagcompound.setDouble("powerQuery[" + i + "]", powerQuery[i]);
			nbttagcompound.setDouble("nextPowerQuery[" + i + "]", nextPowerQuery[i]);
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

	/**
	 * This can be use to provide a rough estimate of how much power is flowing
	 * through a pipe. Measured in MJ/t.
	 *
	 * @return MJ/t
	 */
	public double getCurrentPowerTransferRate() {
		return highestPower;
	}

	/**
	 * This can be use to provide a rough estimate of how much power is
	 * contained in a pipe. Measured in MJ.
	 *
	 * Max should be around (throughput * internalPower.length * 2), ie 112 MJ for a Cobblestone Pipe.
	 *
	 * @return MJ
	 */
	public double getCurrentPowerAmount() {
		double amount = 0.0;
		for (double d : internalPower) {
			amount += d;
		}
		for (double d : internalNextPower) {
			amount += d;
		}
		return amount;
	}

	public float getPistonStage (int i) {
		if (movementStage [i] < 0.5F) {
			return movementStage [i] * 2;
		} else {
			return 1 - (movementStage [i] - 0.5F) * 2;
		}
	}

	public double clearInstantPower () {
		double amount = 0.0;

		for (int i = 0; i < internalPower.length; ++i) {
			amount += internalPower [i];
			internalPower [i] = 0;
		}

		return amount;
	}

	static {
		powerCapacities.put(PipePowerCobblestone.class, 8);
		powerCapacities.put(PipePowerStone.class, 16);
		powerCapacities.put(PipePowerWood.class, 32);
		powerCapacities.put(PipePowerQuartz.class, 64);
		powerCapacities.put(PipePowerIron.class, 128);
		powerCapacities.put(PipePowerGold.class, 256);
		powerCapacities.put(PipePowerDiamond.class, 1024);
		powerCapacities.put(PipePowerHeat.class, 1024);
	}
}
