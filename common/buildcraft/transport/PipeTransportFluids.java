/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.BitSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidEvent.FluidMotionEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.IMachine;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.network.PacketFluidUpdate;

public class PipeTransportFluids extends PipeTransport implements IFluidHandler {

	public class PipeSection extends FluidTank {

		private short currentTime = 0;
		// Tracks how much of the liquid is inbound in timeslots
		private short[] incomming = new short[travelDelay];

		// Tracks how much is currently available (has spent it's inbound delaytime)
		public PipeSection(int capacity) {
			super(null, capacity);
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource == null)
				return 0;

			int maxToFill = Math.min(resource.amount, flowRate - incomming[currentTime]);
			if (maxToFill <= 0)
				return 0;

			FluidStack stackToFill = resource.copy();
			stackToFill.amount = maxToFill;
			int filled = super.fill(stackToFill, doFill);

			if (doFill) {
				incomming[currentTime] += filled;
			}

			return filled;
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			int maxToDrain = Math.min(maxDrain, Math.min(flowRate, getAvailable()));
			if (maxToDrain < 0)
				return null;

			FluidStack drained = super.drain(maxToDrain, doDrain);
			if (drained == null)
				return null;

			return drained;
		}

		public void moveFluids() {
			// Processes the inbound liquid
			incomming[currentTime] = 0;
		}

		public void setTime(short newTime) {
			currentTime = newTime;
		}

		public void reset() {
			this.setFluid(null);
			incomming = new short[travelDelay];
		}

		public int getAvailable() {
			int all = this.getFluid() != null ? this.getFluid().amount : 0;
			for (short slot : incomming) {
				all -= slot;
			}
			return all;
		}

		@Override
		public FluidTank readFromNBT(NBTTagCompound compoundTag) {
			this.setCapacity(compoundTag.getInteger("capacity"));

			for (int i = 0; i < travelDelay; ++i) {
				incomming[i] = compoundTag.getShort("in[" + i + "]");
			}
			setFluid(FluidStack.loadFluidStackFromNBT(compoundTag));
			return this;
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound subTag) {
			subTag.setInteger("capacity", this.getCapacity());

			for (int i = 0; i < travelDelay; ++i) {
				incomming[i] = subTag.getShort("in[" + i + "]");
			}

			if (this.getFluid() != null) {
				this.getFluid().writeToNBT(subTag);
			}
			return subTag;
		}
	}

	public enum TransferState {

		None, Input, Output
	}
	/**
	 * The amount of liquid contained by a pipe section. For simplicity, all
	 * pipe sections are assumed to be of the same volume.
	 */
	public static int LIQUID_IN_PIPE = FluidContainerRegistry.BUCKET_VOLUME / 4;
	public static short INPUT_TTL = 60; // 100
	public static short OUTPUT_TTL = 80; // 80
	public static short OUTPUT_COOLDOWN = 30; // 30
	private static final ForgeDirection[] directions = ForgeDirection.VALID_DIRECTIONS;
	private static final ForgeDirection[] orientations = ForgeDirection.values();
	public byte initClient = 0;
	public short travelDelay = 12;
	public short flowRate = 10;
	public FluidStack[] renderCache = new FluidStack[orientations.length];
	public int[] colorRenderCache = new int[orientations.length];
	public final PipeSection[] internalTanks = new PipeSection[orientations.length];
	private final TransferState[] transferState = new TransferState[directions.length];
	private final int[] inputPerTick = new int[directions.length];
	private final short[] inputTTL = new short[]{0, 0, 0, 0, 0, 0};
	private final short[] outputTTL = new short[]{OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL};
	private final short[] outputCooldown = new short[]{0, 0, 0, 0, 0, 0};
	private final SafeTimeTracker tracker = new SafeTimeTracker();
	private int clientSyncCounter = 0;

	public PipeTransportFluids() {
		for (ForgeDirection direction : orientations) {
			internalTanks[direction.ordinal()] = new PipeSection(getCapacity());
			if (direction != ForgeDirection.UNKNOWN) {
				transferState[direction.ordinal()] = TransferState.None;
			}
		}
	}

	@Override
	public PipeType getPipeType() {
		return PipeType.FLUID;
	}

	public int getCapacity() {
		return LIQUID_IN_PIPE;
	}

	public boolean canReceiveFluid(ForgeDirection o) {
		TileEntity entity = container.getTile(o);

		if (!container.isPipeConnected(o))
			return false;

		if (entity instanceof TileGenericPipe) {
			Pipe pipe = ((TileGenericPipe) entity).pipe;

			if (pipe == null || !pipe.inputOpen(o.getOpposite()))
				return false;
		}

		if (entity instanceof IFluidHandler)
			return true;

		return false;
	}

	@Override
	public void updateEntity() {
		if (CoreProxy.proxy.isRenderWorld(container.worldObj))
			return;

		moveFluids();

		if (tracker.markTimeIfDelay(container.worldObj, BuildCraftCore.updateFactor)) {

			boolean init = false;
			if (++clientSyncCounter > BuildCraftCore.longUpdateFactor) {
				clientSyncCounter = 0;
				init = true;
			}
			PacketFluidUpdate packet = computeFluidUpdate(init, true);
			if (packet != null) {
				CoreProxy.proxy.sendToPlayers(packet.getPacket(), container.worldObj, container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
			}
		}
	}

	/**
	 * Computes the PacketFluidUpdate packet for transmission to a client
	 *
	 * @param initPacket everything is sent, no delta stuff ( first packet )
	 * @param persistChange The render cache change is persisted
	 * @return PacketFluidUpdate liquid update packet
	 */
	private PacketFluidUpdate computeFluidUpdate(boolean initPacket, boolean persistChange) {

		boolean changed = false;
		BitSet delta = new BitSet(21);

		if (initClient > 0) {
			initClient--;
			if (initClient == 1) {
				changed = true;
				delta.set(0, 21);
			}
		}

		FluidStack[] renderCache = this.renderCache.clone();
		int[] colorRenderCache = this.colorRenderCache.clone();

		for (ForgeDirection dir : orientations) {
			FluidStack current = internalTanks[dir.ordinal()].getFluid();
			FluidStack prev = renderCache[dir.ordinal()];

			if (prev == null && current == null) {
				continue;
			}

			if (prev == null && current != null) {
				changed = true;
				renderCache[dir.ordinal()] = current.copy();
				colorRenderCache[dir.ordinal()] = current.getFluid().getColor(current);
				delta.set(dir.ordinal() * 3 + 0);
				delta.set(dir.ordinal() * 3 + 1);
				delta.set(dir.ordinal() * 3 + 2);
				continue;
			}

			if (prev != null && current == null) {
				changed = true;
				renderCache[dir.ordinal()] = null;
				colorRenderCache[dir.ordinal()] = 0xFFFFFF;
				delta.set(dir.ordinal() * 3 + 0);
				delta.set(dir.ordinal() * 3 + 1);
				delta.set(dir.ordinal() * 3 + 2);
				continue;
			}

			if (!prev.equals(current) || initPacket) {
				changed = true;
				renderCache[dir.ordinal()] = current;
				colorRenderCache[dir.ordinal()] = current.getFluid().getColor(current);
				delta.set(dir.ordinal() * 3 + 0);
				delta.set(dir.ordinal() * 3 + 1);
			}

			int displayQty = (prev.amount * 4 + current.amount) / 5;
			if (displayQty == 0 && current.amount > 0 || initPacket) {
				displayQty = current.amount;
			}
			displayQty = Math.min(getCapacity(), displayQty);

			if (prev.amount != displayQty || initPacket) {
				changed = true;
				renderCache[dir.ordinal()].amount = displayQty;
				delta.set(dir.ordinal() * 3 + 2);
			}
		}

		if (persistChange) {
			this.renderCache = renderCache;
			this.colorRenderCache = colorRenderCache;
		}

		if (changed || initPacket) {
			PacketFluidUpdate packet = new PacketFluidUpdate(container.xCoord, container.yCoord, container.zCoord, initPacket);
			packet.renderCache = renderCache;
			packet.colorRenderCache = colorRenderCache;
			packet.delta = delta;
			return packet;
		}

		return null;

	}

	/**
	 * Initializes client
	 */
	@Override
	public void sendDescriptionPacket() {
		super.sendDescriptionPacket();

		initClient = 6;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (ForgeDirection direction : orientations) {
			if (nbttagcompound.hasKey("tank[" + direction.ordinal() + "]")) {
				internalTanks[direction.ordinal()].readFromNBT(nbttagcompound.getCompoundTag("tank[" + direction.ordinal() + "]"));
			}
			if (direction != ForgeDirection.UNKNOWN) {
				transferState[direction.ordinal()] = TransferState.values()[nbttagcompound.getShort("transferState[" + direction.ordinal() + "]")];
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (ForgeDirection direction : orientations) {
			NBTTagCompound subTag = new NBTTagCompound();
			internalTanks[direction.ordinal()].writeToNBT(subTag);
			nbttagcompound.setTag("tank[" + direction.ordinal() + "]", subTag);
			if (direction != ForgeDirection.UNKNOWN) {
				nbttagcompound.setShort("transferState[" + direction.ordinal() + "]", (short) transferState[direction.ordinal()].ordinal());
			}
		}
	}

	private void moveFluids() {
		short newTimeSlot = (short) (container.worldObj.getTotalWorldTime() % travelDelay);

		short outputCount = computeCurrentConnectionStatesAndTickFlows(newTimeSlot);
		moveFromPipe(outputCount);
		moveFromCenter(outputCount);
		moveToCenter();
	}

	private void moveFromPipe(short outputCount) {
		// Move liquid from the non-center to the connected output blocks
		if (outputCount > 0) {
			for (ForgeDirection o : directions) {
				if (transferState[o.ordinal()] == TransferState.Output) {
					TileEntity target = this.container.getTile(o);
					if (!(target instanceof IFluidHandler)) {
						continue;
					}

					FluidStack liquidToPush = internalTanks[o.ordinal()].drain(flowRate, false);
					if (liquidToPush != null && liquidToPush.amount > 0) {
						int filled = ((IFluidHandler) target).fill(o.getOpposite(), liquidToPush, true);
						internalTanks[o.ordinal()].drain(filled, true);
						if (filled <= 0) {
							outputTTL[o.ordinal()]--;
						} else
							FluidEvent.fireEvent(new FluidMotionEvent(liquidToPush, container.worldObj, container.xCoord, container.yCoord, container.zCoord));
					}
				}
			}
		}
	}

	private void moveFromCenter(short outputCount) {
		// Split liquids moving to output equally based on flowrate, how much each side can accept and available liquid
		FluidStack pushStack = internalTanks[ForgeDirection.UNKNOWN.ordinal()].getFluid();
		int totalAvailable = internalTanks[ForgeDirection.UNKNOWN.ordinal()].getAvailable();
		if (totalAvailable < 1)
			return;
		if (pushStack != null) {
			FluidStack testStack = pushStack.copy();
			testStack.amount = flowRate;
			// Move liquid from the center to the output sides
			for (ForgeDirection direction : directions) {
				if (transferState[direction.ordinal()] == TransferState.Output) {
					int available = internalTanks[direction.ordinal()].fill(testStack, false);
					int ammountToPush = (int) (available / (double) flowRate / outputCount * Math.min(flowRate, totalAvailable));
					if (ammountToPush < 1) {
						ammountToPush++;
					}

					FluidStack liquidToPush = internalTanks[ForgeDirection.UNKNOWN.ordinal()].drain(ammountToPush, false);
					if (liquidToPush != null) {
						int filled = internalTanks[direction.ordinal()].fill(liquidToPush, true);
						internalTanks[ForgeDirection.UNKNOWN.ordinal()].drain(filled, true);
						if (filled > 0)
							FluidEvent.fireEvent(new FluidMotionEvent(liquidToPush, container.worldObj, container.xCoord, container.yCoord, container.zCoord));
					}
				}
			}
		}
	}

	private void moveToCenter() {
		int transferInCount = 0;
		FluidStack stackInCenter = internalTanks[ForgeDirection.UNKNOWN.ordinal()].drain(flowRate, false);
		int spaceAvailable = internalTanks[ForgeDirection.UNKNOWN.ordinal()].getCapacity();
		if (stackInCenter != null) {
			spaceAvailable -= stackInCenter.amount;
		}

		for (ForgeDirection dir : directions) {
			inputPerTick[dir.ordinal()] = 0;
			if (transferState[dir.ordinal()] == TransferState.Output) {
				continue;
			}
			FluidStack testStack = internalTanks[dir.ordinal()].drain(flowRate, false);
			if (testStack == null) {
				continue;
			}
			if (stackInCenter != null && !stackInCenter.isFluidEqual(testStack)) {
				continue;
			}
			inputPerTick[dir.ordinal()] = testStack.amount;
			transferInCount++;
		}

		for (ForgeDirection dir : directions) {
			// Move liquid from input sides to the center
			if (transferState[dir.ordinal()] != TransferState.Output && inputPerTick[dir.ordinal()] > 0) {

				int ammountToDrain = (int) ((double) inputPerTick[dir.ordinal()] / (double) flowRate / transferInCount * Math.min(flowRate, spaceAvailable));
				if (ammountToDrain < 1) {
					ammountToDrain++;
				}

				FluidStack liquidToPush = internalTanks[dir.ordinal()].drain(ammountToDrain, false);
				if (liquidToPush != null) {
					int filled = internalTanks[ForgeDirection.UNKNOWN.ordinal()].fill(liquidToPush, true);
					internalTanks[dir.ordinal()].drain(filled, true);
					if (filled > 0)
						FluidEvent.fireEvent(new FluidMotionEvent(liquidToPush, container.worldObj, container.xCoord, container.yCoord, container.zCoord));
				}
			}
		}
	}

	private short computeCurrentConnectionStatesAndTickFlows(short newTimeSlot) {
		short outputCount = 0;

		// Processes all internal tanks
		for (ForgeDirection direction : orientations) {
			internalTanks[direction.ordinal()].setTime(newTimeSlot);
			internalTanks[direction.ordinal()].moveFluids();
			// Input processing
			if (direction == ForgeDirection.UNKNOWN) {
				continue;
			}
			if (transferState[direction.ordinal()] == TransferState.Input) {
				inputTTL[direction.ordinal()]--;
				if (inputTTL[direction.ordinal()] <= 0) {
					transferState[direction.ordinal()] = TransferState.None;
				}
				continue;
			}
			if (!container.pipe.outputOpen(direction)) {
				transferState[direction.ordinal()] = TransferState.None;
				continue;
			}
			if (outputCooldown[direction.ordinal()] > 0) {
				outputCooldown[direction.ordinal()]--;
				continue;
			}
			if (outputTTL[direction.ordinal()] <= 0) {
				transferState[direction.ordinal()] = TransferState.None;
				outputCooldown[direction.ordinal()] = OUTPUT_COOLDOWN;
				outputTTL[direction.ordinal()] = OUTPUT_TTL;
				continue;
			}
			if (canReceiveFluid(direction)) {
				transferState[direction.ordinal()] = TransferState.Output;
				outputCount++;
			}
		}
		return outputCount;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		for (ForgeDirection direction : directions) {
			if (!container.isPipeConnected(direction)) {
				internalTanks[direction.ordinal()].reset();
				transferState[direction.ordinal()] = TransferState.None;
				renderCache[direction.ordinal()] = null;
				colorRenderCache[direction.ordinal()] = 0xFFFFFF;
			}
		}
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportFluids))
				return false;
		}

		if (tile instanceof IFluidHandler) {
			IFluidHandler liq = (IFluidHandler) tile;

			FluidTankInfo[] tankInfo = liq.getTankInfo(side.getOpposite());
			if (tankInfo != null && tankInfo.length > 0)
				return true;
		}

		return tile instanceof TileGenericPipe || (tile instanceof IMachine && ((IMachine) tile).manageFluids());
	}

	public boolean isTriggerActive(ITrigger trigger) {
		return false;
	}

	/**
	 * ITankContainer implementation *
	 */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return fill(from.ordinal(), resource, doFill);
	}

	private int fill(int tankIndex, FluidStack resource, boolean doFill) {
		int filled;

		if (this.container.pipe instanceof IPipeTransportFluidsHook) {
			filled = ((IPipeTransportFluidsHook) this.container.pipe).fill(orientations[tankIndex], resource, doFill);
		} else {
			filled = internalTanks[tankIndex].fill(resource, doFill);
		}

		if (filled > 0 && doFill && tankIndex != ForgeDirection.UNKNOWN.ordinal()) {
			transferState[tankIndex] = TransferState.Input;
			inputTTL[tankIndex] = INPUT_TTL;
		}
		return filled;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{new FluidTankInfo(internalTanks[from.ordinal()])};
	}
}
