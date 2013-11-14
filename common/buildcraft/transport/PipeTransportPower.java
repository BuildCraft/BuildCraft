/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.PipePowerCobblestone;
import buildcraft.transport.pipes.PipePowerDiamond;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerIron;
import buildcraft.transport.pipes.PipePowerQuartz;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerWood;

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
		powerCapacities.put(PipePowerIron.class, 128);
		powerCapacities.put(PipePowerGold.class, 256);
		powerCapacities.put(PipePowerDiamond.class, 1024);
	}
	private boolean needsInit = true;
	private TileEntity[] tiles = new TileEntity[6];
	public float[] displayPower = new float[6];
	private float[] prevDisplayPower = new float[6];
	public short[] clientDisplayPower = new short[6];
	public int overload;
	private int[] powerQuery = new int[6];
	public int[] nextPowerQuery = new int[6];
	private long currentDate;
	private float[] internalPower = new float[6];
	public float[] internalNextPower = new float[6];
	public int maxPower = 8;
	SafeTimeTracker tracker = new SafeTimeTracker();

	public PipeTransportPower() {
		for (int i = 0; i < 6; ++i) {
			powerQuery[i] = 0;
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
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportPower))
				return false;
			return true;
		}

		if (tile instanceof IPowerReceptor) {
			IPowerReceptor receptor = (IPowerReceptor) tile;
			PowerReceiver receiver = receptor.getPowerReceiver(side.getOpposite());
			if (receiver != null && receiver.getType().canReceiveFromPipes())
				return true;
		}

		if (container.pipe instanceof PipePowerWood && tile instanceof IPowerEmitter) {
			IPowerEmitter emitter = (IPowerEmitter) tile;
			if (emitter.canEmitPowerFrom(side.getOpposite()))
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
		if (CoreProxy.proxy.isRenderWorld(container.worldObj))
			return;

		step();

		init();

		// Send the power to nearby pipes who requested it

		System.arraycopy(displayPower, 0, prevDisplayPower, 0, 6);
		Arrays.fill(displayPower, 0.0F);

		for (int i = 0; i < 6; ++i) {
			if (internalPower[i] > 0) {
				float totalPowerQuery = 0;

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0)
						if (tiles[j] instanceof TileGenericPipe || tiles[j] instanceof IPowerReceptor) {
							totalPowerQuery += powerQuery[j];
						}
				}

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0) {
						float watts = 0.0F;

						PowerReceiver prov = getReceiverOnSide(ForgeDirection.VALID_DIRECTIONS[j]);
						if (prov != null && prov.powerRequest() > 0) {
							watts = (internalPower[i] / totalPowerQuery) * powerQuery[j];
							watts = prov.receiveEnergy(Type.PIPE, watts, ForgeDirection.VALID_DIRECTIONS[j].getOpposite());
							internalPower[i] -= watts;
						} else if (tiles[j] instanceof TileGenericPipe) {
							watts = (internalPower[i] / totalPowerQuery) * powerQuery[j];
							TileGenericPipe nearbyTile = (TileGenericPipe) tiles[j];

							PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;

							watts = nearbyTransport.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[j].getOpposite(), watts);
							internalPower[i] -= watts;
						}

						displayPower[j] += watts;
						displayPower[i] += watts;
					}
				}
			}
		}

		double highestPower = 0;
		for (int i = 0; i < 6; i++) {
			displayPower[i] = (prevDisplayPower[i] * (DISPLAY_SMOOTHING - 1.0F) + displayPower[i]) / DISPLAY_SMOOTHING;
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

		// Compute the tiles requesting energy that are not power pipes

		for (int i = 0; i < 6; ++i) {
			PowerReceiver prov = getReceiverOnSide(ForgeDirection.VALID_DIRECTIONS[i]);
			if (prov != null) {
				float request = prov.powerRequest();

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

		if (tracker.markTimeIfDelay(container.worldObj, 2 * BuildCraftCore.updateFactor)) {
			PacketPowerUpdate packet = new PacketPowerUpdate(container.xCoord, container.yCoord, container.zCoord);

			double displayFactor = MAX_DISPLAY / 1024.0;
			for (int i = 0; i < clientDisplayPower.length; i++) {
				clientDisplayPower[i] = (short) (displayPower[i] * displayFactor + .9999);
			}

			packet.displayPower = clientDisplayPower;
			packet.overload = isOverloaded();
			CoreProxy.proxy.sendToPlayers(packet.getPacket(), container.worldObj, container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
		}

	}

	private PowerReceiver getReceiverOnSide(ForgeDirection side) {
		TileEntity tile = tiles[side.ordinal()];
		if (!(tile instanceof IPowerReceptor))
			return null;
		IPowerReceptor receptor = (IPowerReceptor) tile;
		PowerReceiver receiver = receptor.getPowerReceiver(side.getOpposite());
		if (receiver == null)
			return null;
		if (!receiver.getType().canReceiveFromPipes())
			return null;
		return receiver;
	}

	public boolean isOverloaded() {
		return overload >= OVERLOAD_TICKS;
	}

	private void step() {
		if (currentDate != container.worldObj.getTotalWorldTime()) {
			currentDate = container.worldObj.getTotalWorldTime();

			powerQuery = nextPowerQuery;
			nextPowerQuery = new int[6];

			float[] next = internalPower;
			internalPower = internalNextPower;
			internalNextPower = next;
//			for (int i = 0; i < powerQuery.length; i++) {
//				int sum = 0;
//				for (int j = 0; j < powerQuery.length; j++) {
//					if (i != j) {
//						sum += powerQuery[j];
//					}
//				}
//				if (sum == 0 && internalNextPower[i] > 0) {
//					internalNextPower[i] -= 1;
//				}
//			}
		}
	}

	/**
	 * Do NOT ever call this from outside Buildcraft. It is NOT part of the API.
	 * All power input MUST go through designated input pipes, such as Wooden
	 * Power Pipes or a subclass thereof.
	 */
	public float receiveEnergy(ForgeDirection from, float val) {

		// Keep this in reserve for if too many idiots start bypassing the API
		// Verify that it is BC calling this method. 
		// If its someone else take all their power and run!
		// Note: This should be safe for PipePowerWood subclasses, aka custom input pipes.
//		StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
//		String caller = stackTrace[1].getClassName();
//		if (!caller.equals("buildcraft.transport.PipeTransportPower")
//				&& !caller.equals("buildcraft.transport.pipes.PipePowerWood")) {
//			 return val;
//		}

		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			float ret = ((IPipeTransportPowerHook) this.container.pipe).receiveEnergy(from, val);
			if (ret >= 0)
				return ret;
		}
		int side = from.ordinal();
		if (internalNextPower[side] > maxPower)
			return 0;

		internalNextPower[side] += val;

		if (internalNextPower[side] > maxPower) {
			val -= internalNextPower[side] - maxPower;
			internalNextPower[side] = maxPower;
			if (val < 0)
				val = 0;
		}
		return val;
	}

	public void requestEnergy(ForgeDirection from, float amount) {
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			nextPowerQuery[from.ordinal()] += ((IPipeTransportPowerHook) this.container.pipe).requestEnergy(from, amount);
		} else {
			nextPowerQuery[from.ordinal()] += amount;
		}
	}

	@Override
	public void initialize() {
		currentDate = container.worldObj.getTotalWorldTime();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			powerQuery[i] = nbttagcompound.getInteger("powerQuery[" + i + "]");
			nextPowerQuery[i] = nbttagcompound.getInteger("nextPowerQuery[" + i + "]");
			internalPower[i] = (float) nbttagcompound.getDouble("internalPower[" + i + "]");
			internalNextPower[i] = (float) nbttagcompound.getDouble("internalNextPower[" + i + "]");
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
