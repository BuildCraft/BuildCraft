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
import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyReceiver;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.IEngine;
import buildcraft.api.transport.IPipeContainer;
import buildcraft.core.DefaultProps;
import buildcraft.core.TileBuildCraft;
import buildcraft.transport.network.PacketPowerUpdate;
import buildcraft.transport.pipes.PipePowerCobblestone;
import buildcraft.transport.pipes.PipePowerDiamond;
import buildcraft.transport.pipes.PipePowerEmerald;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerIron;
import buildcraft.transport.pipes.PipePowerQuartz;
import buildcraft.transport.pipes.PipePowerSandstone;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerWood;

public class PipeTransportPower extends PipeTransport {

	public static final Map<Class<? extends Pipe<?>>, Integer> powerCapacities = new HashMap<Class<? extends Pipe<?>>, Integer>();

	private static final int DISPLAY_SMOOTHING = 10;
	private static final int OVERLOAD_TICKS = 60;

	public short[] displayPower = new short[6];
	public int overload;
	public int[] nextPowerQuery = new int[6];
	public int[] internalNextPower = new int[6];
	public int maxPower = 80;
	public float[] movementStage = new float[] {0, 0, 0};

	private boolean needsInit = true;
	private TileEntity[] tiles = new TileEntity[6];

	private short[] prevDisplayPower = new short[6];

	private int[] powerQuery = new int[6];

	private long currentDate;
	private int[] internalPower = new int[6];
	private int[] externalPower = new int[6];

	private int highestPower;
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
	public IPipeContainer.PipeType getPipeType() {
		return IPipeContainer.PipeType.POWER;
	}

	public void initFromPipe(Class<? extends Pipe> pipeClass) {
		maxPower = powerCapacities.get(pipeClass);
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe<?> pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportPower)) {
				return false;
			}
			return true;
		}

		if (container.pipe instanceof PipePowerWood) {
			return isPowerSource(tile, side);
		} else {
			if (tile instanceof IEngine) {
				// Disregard engines for this.
				return false;
			}
			if (tile instanceof IEnergyHandler || tile instanceof IEnergyReceiver) {
				IEnergyConnection handler = (IEnergyConnection) tile;
				if (handler.canConnectEnergy(side.getOpposite())) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean isPowerSource(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileBuildCraft && !(tile instanceof IEngine)) {
			// Disregard non-engine BC tiles.
			// While this, of course, does nothing to work with other mods,
			// it at least makes it work nicely with BC's built-in blocks while
			// the new RF api isn't out.
			return false;
		}

		return tile instanceof IEnergyConnection && ((IEnergyConnection) tile).canConnectEnergy(side.getOpposite());
		// TODO: Look into this code again when the new RF API is out.
		/*
		if (tile instanceof IEnergyConnection && ((IEnergyConnection) tile).canConnectEnergy(side.getOpposite())) {
			if (tile instanceof TileBuildCraft && !(tile instanceof IEngine)) {
				// Disregard non-engine BC tiles
				return false;
			}
			// Disregard tiles which are consumers but NOT providers
			return (tile instanceof IEngine) || (tile instanceof IEnergyHandler);
		} else {
			// Disregard tiles which can't connect either, I guess.
			return false;
		}*/
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            updateTile(side);
        }
	}

    private void updateTile(ForgeDirection side) {
        TileEntity tile = container.getTile(side);
        if (tile != null && container.isPipeConnected(side)) {
            tiles[side.ordinal()] = tile;
        } else {
            tiles[side.ordinal()] = null;
            internalPower[side.ordinal()] = 0;
            internalNextPower[side.ordinal()] = 0;
            displayPower[side.ordinal()] = 0;
        }
    }

	private void init() {
		if (needsInit) {
			needsInit = false;
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                updateTile(side);
            }
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

        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if (tiles[side.ordinal()] != null && tiles[side.ordinal()].isInvalid()) {
                updateTile(side);
            }
        }

		// Send the power to nearby pipes who requested it

		System.arraycopy(displayPower, 0, prevDisplayPower, 0, 6);
		Arrays.fill(displayPower, (short) 0);

		// STEP 1 - computes the total amount of power contained and total
		// amount of power queried

		int totalPowerContained = 0;
		int totalPowerQuery = 0;

		for (int dir = 0; dir < 6; ++dir) {
			totalPowerContained += internalPower[dir];
			if (internalPower[dir] == 0) {
				totalPowerQuery += powerQuery[dir];
			}
		}

		// STEP 2 - sends the power to all directions and computes the actual
		// amount of power that was consumed

		int totalPowerConsumed = 0;

		if (totalPowerContained > 0) {
			for (int out = 0; out < 6; ++out) {
				externalPower[out] = 0;

				if (powerQuery[out] > 0 && internalPower[out] == 0) {
					int powerConsumed = powerQuery[out] * totalPowerContained / totalPowerQuery;
					boolean tilePowered = false;

					if (tiles[out] instanceof TileGenericPipe) {
						// Transmit power to the nearby pipe

						TileGenericPipe nearbyTile = (TileGenericPipe) tiles[out];
						PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;
						powerConsumed = nearbyTransport.receiveEnergy(
								ForgeDirection.VALID_DIRECTIONS[out].getOpposite(),
								powerConsumed);
						tilePowered = true;
					} else if (tiles[out] instanceof IEnergyHandler) {
						IEnergyHandler handler = (IEnergyHandler) tiles[out];

						if (handler.canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[out].getOpposite())) {
							// Transmit power to an RF energy handler

							powerConsumed = handler.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[out].getOpposite(),
									powerConsumed, false);
							tilePowered = true;
						}
					} else if (tiles[out] instanceof IEnergyReceiver) {
						IEnergyReceiver handler = (IEnergyReceiver) tiles[out];

						if (handler.canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[out].getOpposite())) {
							// Transmit power to an RF energy handler

							powerConsumed = handler.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[out].getOpposite(),
									powerConsumed, false);
							tilePowered = true;
						}
					}

					if (!tilePowered) {
						externalPower[out] = powerConsumed;
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
				int powerConsumed = internalPower[in] * totalPowerConsumed / totalPowerContained;
				displayPower[in] += powerConsumed;
			}
		}

		// NEXT STEPS... other things to do...

		highestPower = 0;
		for (int i = 0; i < 6; i++) {
			displayPower[i] = (short) ((prevDisplayPower[i] * (DISPLAY_SMOOTHING - 1.0F) + displayPower[i]) / DISPLAY_SMOOTHING);

			if (displayPower[i] > highestPower) {
				highestPower = displayPower[i];
			}

			if (displayPower[i] < 0) {
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
		    if (!outputOpen(dir)) {
			    continue;
			}

			TileEntity tile = tiles [dir.ordinal()];
			
		    if (tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportPower) {
		    	continue;
		    }
		    
			if (tile instanceof IEnergyHandler) {
				IEnergyHandler handler = (IEnergyHandler) tile;
				if (handler.canConnectEnergy(dir.getOpposite())) {
					int request = handler.receiveEnergy(dir.getOpposite(), this.maxPower, true);

					if (request > 0) {
						requestEnergy(dir, request);
					}
				}
			} else if (tile instanceof IEnergyReceiver) {
				IEnergyReceiver handler = (IEnergyReceiver) tile;
				if (handler.canConnectEnergy(dir.getOpposite())) {
					int request = handler.receiveEnergy(dir.getOpposite(), this.maxPower, true);

					if (request > 0) {
						requestEnergy(dir, request);
					}
				}
			}
		}

		// Sum the amount of energy requested on each side

		int[] transferQuery = new int[6];

		for (int i = 0; i < 6; ++i) {
			transferQuery[i] = 0;

			if (!inputOpen(ForgeDirection.getOrientation(i))) {
			    continue;
			}

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

			packet.displayPower = displayPower;
			packet.overload = isOverloaded();
			BuildCraftTransport.instance.sendToPlayers(packet, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
		}
	}

	public boolean isOverloaded() {
		return overload >= OVERLOAD_TICKS;
	}

	private void step() {
		if (container != null && container.getWorldObj() != null
				&& currentDate != container.getWorldObj().getTotalWorldTime()) {
			currentDate = container.getWorldObj().getTotalWorldTime();

			powerQuery = nextPowerQuery;
			nextPowerQuery = new int[6];

			internalPower = internalNextPower;
			internalNextPower = new int[6];

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
	public int receiveEnergy(ForgeDirection from, int valI) {
		int val = valI;
		step();
		if (this.container.pipe instanceof IPipeTransportPowerHook) {
			int ret = ((IPipeTransportPowerHook) this.container.pipe).receiveEnergy(from, val);
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

	public void requestEnergy(ForgeDirection from, int amount) {
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
			powerQuery[i] = nbttagcompound.getInteger("powerQuery[" + i + "]");
			nextPowerQuery[i] = nbttagcompound.getInteger("nextPowerQuery[" + i + "]");
			internalPower[i] = nbttagcompound.getInteger("internalPower[" + i + "]");
			internalNextPower[i] = nbttagcompound.getInteger("internalNextPower[" + i + "]");
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			nbttagcompound.setInteger("powerQuery[" + i + "]", powerQuery[i]);
			nbttagcompound.setInteger("nextPowerQuery[" + i + "]", nextPowerQuery[i]);
			nbttagcompound.setInteger("internalPower[" + i + "]", internalPower[i]);
			nbttagcompound.setInteger("internalNextPower[" + i + "]", internalNextPower[i]);
		}
	}

	/**
	 * Client-side handler for receiving power updates from the server;
	 *
	 * @param packetPower
	 */
	public void handlePowerPacket(PacketPowerUpdate packetPower) {
		displayPower = packetPower.displayPower;
		overload = packetPower.overload ? OVERLOAD_TICKS : 0;
	}

	/**
	 * This can be use to provide a rough estimate of how much power is flowing
	 * through a pipe. Measured in RF/t.
	 *
	 * @return RF/t
	 */
	public int getCurrentPowerTransferRate() {
		return highestPower;
	}

	/**
	 * This can be use to provide a rough estimate of how much power is
	 * contained in a pipe. Measured in RF.
	 *
	 * Max should be around (throughput * internalPower.length * 2), ie 112 MJ for a Cobblestone Pipe.
	 *
	 * @return RF
	 */
	public int getCurrentPowerAmount() {
		int amount = 0;
		for (int d : internalPower) {
			amount += d;
		}
		for (int d : internalNextPower) {
			amount += d;
		}
		return amount;
	}

	public float getPistonStage(int i) {
		if (movementStage [i] < 0.5F) {
			return movementStage [i] * 2;
		} else {
			return 1 - (movementStage [i] - 0.5F) * 2;
		}
	}

	public int clearInstantPower() {
		int amount = 0;

		for (int i = 0; i < internalPower.length; ++i) {
			amount += internalPower [i];
			internalPower [i] = 0;
		}

		return amount;
	}

	public int consumePower(ForgeDirection dir, int max) {
		int result;

		if (externalPower[dir.ordinal()] < max) {
			result = externalPower[dir.ordinal()];
		} else {
			result = max;
			externalPower[dir.ordinal()] -= max;
		}

		return result;
	}

	public boolean isQueryingPower() {
		for (int d : powerQuery) {
			if (d > 0) {
				return true;
			}
		}

		return false;
	}

	static {
		powerCapacities.put(PipePowerCobblestone.class, 80);
		powerCapacities.put(PipePowerStone.class, 160);
		powerCapacities.put(PipePowerWood.class, 320);
        powerCapacities.put(PipePowerSandstone.class, 320);
		powerCapacities.put(PipePowerQuartz.class, 640);
		powerCapacities.put(PipePowerIron.class, 1280);
		powerCapacities.put(PipePowerGold.class, 2560);
		powerCapacities.put(PipePowerEmerald.class, 2560);
		powerCapacities.put(PipePowerDiamond.class, 10240);
	}
}
