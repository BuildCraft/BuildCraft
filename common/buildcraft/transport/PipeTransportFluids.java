/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.MathUtils;
import buildcraft.transport.network.PacketFluidUpdate;
import buildcraft.transport.pipes.PipeFluidsCobblestone;
import buildcraft.transport.pipes.PipeFluidsDiamond;
import buildcraft.transport.pipes.PipeFluidsEmerald;
import buildcraft.transport.pipes.PipeFluidsGold;
import buildcraft.transport.pipes.PipeFluidsIron;
import buildcraft.transport.pipes.PipeFluidsQuartz;
import buildcraft.transport.pipes.PipeFluidsSandstone;
import buildcraft.transport.pipes.PipeFluidsStone;
import buildcraft.transport.pipes.PipeFluidsVoid;
import buildcraft.transport.pipes.PipeFluidsWood;

public class PipeTransportFluids extends PipeTransport implements IFluidHandler {
    public static final Map<Class<? extends Pipe<?>>, Integer> fluidCapacities = new HashMap<Class<? extends Pipe<?>>, Integer>();

	public class PipeSection extends FluidTank {

		private short currentTime = 0;
		// Tracks how much of the liquid is inbound in timeslots
	        private short[] incomming;

		// Tracks how much is currently available (has spent it's inbound delaytime)
		public PipeSection(int capacity) {
			super(null, capacity);
			incomming = new short[travelDelay];
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource == null) {
				return 0;
			}

			int maxToFill = Math.min(resource.amount, flowRate - incomming[currentTime]);
			if (maxToFill <= 0) {
				return 0;
			}

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
			int maxToDrain = getAvailable();
			if (maxToDrain > maxDrain) {
				maxToDrain = maxDrain;
			}
			if (maxToDrain > flowRate) {
				maxToDrain = flowRate;
			}
			if (maxToDrain <= 0) {
				return null;
			}
			return super.drain(maxToDrain, doDrain);
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
			int all = getFluidAmount();
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
	private static final EnumFacing[] directions = EnumFacing.values();
	private static final EnumFacing[] orientations = EnumFacing.values();
	public byte initClient = 0;
	public int travelDelay = 12;
	public int flowRate;
	public FluidStack[] renderCache = new FluidStack[orientations.length];
	public int[] colorRenderCache = new int[orientations.length];
	public final PipeSection[] internalTanks = new PipeSection[orientations.length];
	private final TransferState[] transferState = new TransferState[directions.length];
	private final int[] inputPerTick = new int[directions.length];
	private final short[] inputTTL = new short[]{0, 0, 0, 0, 0, 0};
	private final short[] outputTTL = new short[]{OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL};
	private final short[] outputCooldown = new short[]{0, 0, 0, 0, 0, 0};
	private final SafeTimeTracker tracker = new SafeTimeTracker(BuildCraftCore.updateFactor);
	private int clientSyncCounter = 0;

    public PipeTransportFluids() {
		for (EnumFacing direction : orientations) {
			internalTanks[direction.ordinal()] = new PipeSection(getCapacity());
			if (direction != null) {
				transferState[direction.ordinal()] = TransferState.None;
			}
		}
	}

    public void initFromPipe(Class<? extends Pipe> pipeClass) {
	    flowRate = fluidCapacities.get(pipeClass);
	    travelDelay = MathUtils.clamp(Math.round(16 / (flowRate / 10)), 1, 12);
    }

	@Override
	public PipeType getPipeType() {
		return PipeType.FLUID;
	}

	public int getCapacity() {
		return LIQUID_IN_PIPE;
	}

	private boolean canReceiveFluid(EnumFacing o) {
		TileEntity entity = container.getTile(o);

		if (!container.isPipeConnected(o)) {
			return false;
		}

		if (entity instanceof TileGenericPipe) {
			Pipe<?> pipe = ((TileGenericPipe) entity).pipe;

			if (pipe == null || !inputOpen(o.getOpposite())) {
				return false;
			}
		}

		if (entity instanceof IFluidHandler) {
			return true;
		}

		return false;
	}

	@Override
	public void updateEntity() {
		if (container.getWorld().isRemote) {
			return;
		}

		moveFluids();

		if (tracker.markTimeIfDelay(container.getWorld())) {

			boolean init = false;
			if (++clientSyncCounter > BuildCraftCore.longUpdateFactor) {
				clientSyncCounter = 0;
				init = true;
			}
			PacketFluidUpdate packet = computeFluidUpdate(init, true);
			if (packet != null) {
				BuildCraftTransport.instance.sendToPlayersNear(packet, container, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
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
		BitSet delta = new BitSet(PacketFluidUpdate.FLUID_DATA_NUM * EnumFacing.values().length);

		if (initClient > 0) {
			initClient--;
			if (initClient == 1) {
				changed = true;
				delta.set(0, PacketFluidUpdate.FLUID_DATA_NUM * EnumFacing.values().length);
			}
		}

		FluidStack[] renderCacheCopy = this.renderCache.clone();
		int[] colorRenderCacheCopy = this.colorRenderCache.clone();

		for (EnumFacing dir : orientations) {
			FluidStack current = internalTanks[dir.ordinal()].getFluid();
			FluidStack prev = renderCacheCopy[dir.ordinal()];

			if (current != null && current.getFluid() == null) {
				continue;
			}

			if (prev == null && current == null) {
				continue;
			}

			if (prev == null ^ current == null) {
				changed = true;
				if (current != null) {
					renderCacheCopy[dir.ordinal()] = current.copy();
					colorRenderCacheCopy[dir.ordinal()] = current.getFluid().getColor(current);
				} else {
					renderCacheCopy[dir.ordinal()] = null;
					colorRenderCacheCopy[dir.ordinal()] = 0xFFFFFF;
				}
				delta.set(dir.ordinal() * PacketFluidUpdate.FLUID_DATA_NUM + PacketFluidUpdate.FLUID_ID_BIT);
				delta.set(dir.ordinal() * PacketFluidUpdate.FLUID_DATA_NUM + PacketFluidUpdate.FLUID_AMOUNT_BIT);
				continue;
			}

			if (prev == null || current == null) {
				continue;
			}

			if (!prev.equals(current) || initPacket) {
				changed = true;
				renderCacheCopy[dir.ordinal()] = current;
				colorRenderCacheCopy[dir.ordinal()] = current.getFluid().getColor(current);
				delta.set(dir.ordinal() * PacketFluidUpdate.FLUID_DATA_NUM + PacketFluidUpdate.FLUID_ID_BIT);
			}

			int displayQty = (prev.amount * 4 + current.amount) / 5;
			if (displayQty == 0 && current.amount > 0 || initPacket) {
				displayQty = current.amount;
			}
			displayQty = Math.min(getCapacity(), displayQty);

			if (prev.amount != displayQty || initPacket) {
				changed = true;
				renderCacheCopy[dir.ordinal()].amount = displayQty;
				delta.set(dir.ordinal() * PacketFluidUpdate.FLUID_DATA_NUM + PacketFluidUpdate.FLUID_AMOUNT_BIT);
			}
		}

		if (persistChange) {
			this.renderCache = renderCacheCopy;
			this.colorRenderCache = colorRenderCacheCopy;
		}

		if (changed || initPacket) {
			PacketFluidUpdate packet = new PacketFluidUpdate(container.getPos(), initPacket);
			packet.renderCache = renderCacheCopy;
			packet.colorRenderCache = colorRenderCacheCopy;
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

		for (int i = 0; i < 7; i++) {
			if (nbttagcompound.hasKey("tank[" + i + "]")) {
				internalTanks[i].readFromNBT(nbttagcompound.getCompoundTag("tank[" + i + "]"));
			}
			if (i < 6) {
				transferState[i] = TransferState.values()[nbttagcompound.getShort("transferState[" + i + "]")];
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (int i = 0; i < 7; i++) {
			NBTTagCompound subTag = new NBTTagCompound();
			internalTanks[i].writeToNBT(subTag);
			nbttagcompound.setTag("tank[" + i + "]", subTag);
			if (i < 6) {
				nbttagcompound.setShort("transferState[" + i + "]", (short) transferState[i].ordinal());
			}
		}
	}

	private void moveFluids() {
		short newTimeSlot = (short) (container.getWorld().getTotalWorldTime() % travelDelay);

		short outputCount = computeCurrentConnectionStatesAndTickFlows(newTimeSlot > 0 && newTimeSlot < travelDelay ? newTimeSlot : 0);
		moveFromPipe(outputCount);
		moveFromCenter(outputCount);
		moveToCenter();
	}

	private void moveFromPipe(short outputCount) {
		// Move liquid from the non-center to the connected output blocks
		if (outputCount > 0) {
			for (EnumFacing o : directions) {
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
						}
//						else FluidEvent.fireEvent(new FluidMotionEvent(liquidToPush, container.getWorld(), container.xCoord, container.yCoord, container.zCoord));
					}
				}
			}
		}
	}

	private void moveFromCenter(short outputCount) {
		// Split liquids moving to output equally based on flowrate, how much each side can accept and available liquid
		FluidStack pushStack = internalTanks[6].getFluid();
		int totalAvailable = internalTanks[6].getAvailable();
		if (totalAvailable < 1) {
			return;
		}
		if (pushStack != null) {
			FluidStack testStack = pushStack.copy();
			testStack.amount = flowRate;
			// Move liquid from the center to the output sides
			for (EnumFacing direction : directions) {
				if (transferState[direction.ordinal()] == TransferState.Output) {
					int available = internalTanks[direction.ordinal()].fill(testStack, false);
					int ammountToPush = (int) (available / (double) flowRate / outputCount * Math.min(flowRate, totalAvailable));
					if (ammountToPush < 1) {
						ammountToPush++;
					}

					FluidStack liquidToPush = internalTanks[6].drain(ammountToPush, false);
					if (liquidToPush != null) {
						int filled = internalTanks[direction.ordinal()].fill(liquidToPush, true);
						internalTanks[6].drain(filled, true);
//						if (filled > 0)
//							FluidEvent.fireEvent(new FluidMotionEvent(liquidToPush, container.getWorld(), container.xCoord, container.yCoord, container.zCoord));
					}
				}
			}
		}
	}

	private void moveToCenter() {
		int transferInCount = 0;
		FluidStack stackInCenter = internalTanks[6].drain(flowRate, false);
		int spaceAvailable = internalTanks[6].getCapacity();
		if (stackInCenter != null) {
			spaceAvailable -= stackInCenter.amount;
		}

		for (EnumFacing dir : directions) {
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

		for (EnumFacing dir : directions) {
			// Move liquid from input sides to the center
			if (transferState[dir.ordinal()] != TransferState.Output && inputPerTick[dir.ordinal()] > 0) {

				int ammountToDrain = (int) ((double) inputPerTick[dir.ordinal()] / (double) flowRate / transferInCount * Math.min(flowRate, spaceAvailable));
				if (ammountToDrain < 1) {
					ammountToDrain++;
				}

				FluidStack liquidToPush = internalTanks[dir.ordinal()].drain(ammountToDrain, false);
				if (liquidToPush != null) {
					int filled = internalTanks[6].fill(liquidToPush, true);
					internalTanks[dir.ordinal()].drain(filled, true);
//					if (filled > 0)
//						FluidEvent.fireEvent(new FluidMotionEvent(liquidToPush, container.getWorld(), container.xCoord, container.yCoord, container.zCoord));
				}
			}
		}
	}

	private short computeCurrentConnectionStatesAndTickFlows(short newTimeSlot) {
		short outputCount = 0;

		// Processes all internal tanks
		for (EnumFacing direction : orientations) {
			internalTanks[direction.ordinal()].setTime(newTimeSlot);
			internalTanks[direction.ordinal()].moveFluids();
			// Input processing
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
			if (canReceiveFluid(direction) && outputOpen(direction)) {
				transferState[direction.ordinal()] = TransferState.Output;
				outputCount++;
			}
		}

		internalTanks[6].setTime(newTimeSlot);
		internalTanks[6].moveFluids();
		return outputCount;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		for (EnumFacing direction : directions) {
			if (!container.isPipeConnected(direction)) {
				internalTanks[direction.ordinal()].reset();
				transferState[direction.ordinal()] = TransferState.None;
				renderCache[direction.ordinal()] = null;
				colorRenderCache[direction.ordinal()] = 0xFFFFFF;
			}
		}
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
		if (tile instanceof TileGenericPipe) {
			Pipe<?> pipe2 = ((TileGenericPipe) tile).pipe;
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportFluids)) {
				return false;
			}
		}

		if (tile instanceof IFluidHandler) {
			IFluidHandler liq = (IFluidHandler) tile;

			FluidTankInfo[] tankInfo = liq.getTankInfo(side.getOpposite());
			if (tankInfo != null && tankInfo.length > 0) {
				return true;
			}
		}

		return tile instanceof TileGenericPipe;
	}

	/**
	 * ITankContainer implementation *
	 */
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
		return fill(from.ordinal(), resource, doFill);
	}

	private int fill(int tankIndex, FluidStack resource, boolean doFill) {
		EnumFacing d = EnumFacing.getFront(tankIndex);
		if (tankIndex != 6 && !inputOpen(d)) {
			return 0;
		}

		int filled;

		if (this.container.pipe instanceof IPipeTransportFluidsHook) {
			filled = ((IPipeTransportFluidsHook) this.container.pipe).fill(orientations[tankIndex], resource, doFill);
		} else {
			filled = internalTanks[tankIndex].fill(resource, doFill);
		}

		if (filled > 0 && doFill && tankIndex != 6) {
			transferState[tankIndex] = TransferState.Input;
			inputTTL[tankIndex] = INPUT_TTL;
		}

		return filled;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {
	        return inputOpen(from);
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {
		return new FluidTankInfo[]{new FluidTankInfo(internalTanks[from.ordinal()])};
	}

    static {
        fluidCapacities.put(PipeFluidsCobblestone.class, 1 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsDiamond.class, 8 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsEmerald.class, 4 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsGold.class, 8 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsIron.class, 4 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsQuartz.class, 4 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsSandstone.class, 2 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsStone.class, 2 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsVoid.class, 1 * BuildCraftTransport.pipeFluidsBaseFlowRate);
        fluidCapacities.put(PipeFluidsWood.class, 1 * BuildCraftTransport.pipeFluidsBaseFlowRate);
    }
}
