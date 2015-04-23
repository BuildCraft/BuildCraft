package buildcraft.transport;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.utils.MathUtils;
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
import buildcraft.transport.pipes.events.PipeEventFluid;
import buildcraft.transport.utils.FluidRenderData;

public class PipeTransportFluids extends PipeTransport implements IFluidHandler {
	public class PipeSection {
		public int amount;

		private short currentTime = 0;
		private short[] incoming = new short[travelDelay];

		public int fill(int maxFill, boolean doFill) {
			int amountToFill = Math.min(getMaxFillRate(), maxFill);
			if (amountToFill <= 0) {
				return 0;
			}

			if (doFill) {
				incoming[currentTime] += amountToFill;
				amount += amountToFill;
			}
			return amountToFill;
		}

		public int drain(int maxDrain, boolean doDrain) {
			int maxToDrain = getAvailable();
			if (maxToDrain > maxDrain) {
				maxToDrain = maxDrain;
			}
			if (maxToDrain > flowRate) {
				maxToDrain = flowRate;
			}
			if (maxToDrain <= 0) {
				return 0;
			} else {
				if (doDrain) {
					amount -= maxToDrain;
				}
				return maxToDrain;
			}
		}

		public void moveFluids() {
			incoming[currentTime] = 0;
		}

		public void setTime(short newTime) {
			currentTime = newTime;
		}

		public void reset() {
			this.amount = 0;
			incoming = new short[travelDelay];
		}

		/**
		 * Get the amount of fluid available to move. This nicely takes care
		 * of the travel delay mechanic.
		 * @return
		 */
		public int getAvailable() {
			int all = amount;
			for (short slot : incoming) {
				all -= slot;
			}
			return all;
		}

		public int getMaxFillRate() {
			return Math.min(capacity - amount, flowRate - incoming[currentTime]);
		}

		public void readFromNBT(NBTTagCompound compoundTag) {
			this.amount = compoundTag.getShort("capacity");

			for (int i = 0; i < travelDelay; ++i) {
				incoming[i] = compoundTag.getShort("in[" + i + "]");
			}
		}

		public void writeToNBT(NBTTagCompound subTag) {
			subTag.setShort("capacity", (short) amount);

			for (int i = 0; i < travelDelay; ++i) {
				subTag.setShort("in[" + i + "]", incoming[i]);
			}
		}
	}

	public static final Map<Class<? extends Pipe<?>>, Integer> fluidCapacities = new HashMap<Class<? extends Pipe<?>>, Integer>();

	/**
	 * The amount of liquid contained by a pipe section. For simplicity, all
	 * pipe sections are assumed to be of the same volume.
	 */
	public static int LIQUID_IN_PIPE = FluidContainerRegistry.BUCKET_VOLUME / 4;
	public static short INPUT_TTL = 60; // 100
	public static short OUTPUT_TTL = 80; // 80
	public static short OUTPUT_COOLDOWN = 30; // 30

	public PipeSection[] sections = new PipeSection[7];
	public FluidStack fluidType;

	public FluidRenderData renderCache = new FluidRenderData();

	private static final ForgeDirection[] directions = ForgeDirection.VALID_DIRECTIONS;
	private static final ForgeDirection[] orientations = ForgeDirection.values();
	private final SafeTimeTracker networkSyncTracker = new SafeTimeTracker(BuildCraftCore.updateFactor);
	private final TransferState[] transferState = new TransferState[directions.length];
	private final int[] inputPerTick = new int[directions.length];
	private final short[] inputTTL = new short[]{0, 0, 0, 0, 0, 0};
	private final short[] outputTTL = new short[]{OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL};
	private final short[] outputCooldown = new short[]{0, 0, 0, 0, 0, 0};
	private final boolean[] canReceiveCache = new boolean[6];
	private byte initClient = 0;
	private int clientSyncCounter = 0;
	private int capacity, flowRate, travelDelay;

	public enum TransferState {
		None, Input, Output
	}

	public PipeTransportFluids() {
		for (ForgeDirection direction : directions) {
			transferState[direction.ordinal()] = TransferState.None;
		}
	}

	public int getCapacity() {
		return capacity;
	}

	public int getFlowRate() {
		return flowRate;
	}

	public void initFromPipe(Class<? extends Pipe> pipeClass) {
		capacity = LIQUID_IN_PIPE;
		flowRate = fluidCapacities.get(pipeClass);
		travelDelay = MathUtils.clamp(Math.round(16 / (flowRate / 10)), 1, 12);
		for (ForgeDirection direction : orientations) {
			sections[direction.ordinal()] = new PipeSection();
		}
	}

	@Override
	public void initialize() {
		super.initialize();
		
		for (ForgeDirection d : directions) {
			canReceiveCache[d.ordinal()] = canReceiveFluid(d);
		}
	}

	@Override
	public IPipeTile.PipeType getPipeType() {
		return IPipeTile.PipeType.FLUID;
	}

	private boolean canReceiveFluid(ForgeDirection o) {
		TileEntity tile = container.getTile(o);

		if (!container.isPipeConnected(o)) {
			return false;
		}

		if (tile instanceof IPipeTile) {
			Pipe<?> pipe = (Pipe<?>) ((IPipeTile) tile).getPipe();

			if (pipe == null || !inputOpen(o.getOpposite())) {
				return false;
			}
		}

		if (tile instanceof IFluidHandler) {
			return true;
		}

		return false;
	}

	@Override
	public void updateEntity() {
		if (container.getWorldObj().isRemote) {
			return;
		}

		if (fluidType != null) {
			moveFluids();
		}

		if (networkSyncTracker.markTimeIfDelay(container.getWorldObj())) {
			boolean init = false;
			if (++clientSyncCounter > BuildCraftCore.longUpdateFactor) {
				clientSyncCounter = 0;
				init = true;
			}
			PacketFluidUpdate packet = computeFluidUpdate(init, true);

			if (packet != null) {
				BuildCraftTransport.instance.sendToPlayers(packet, container.getWorldObj(), container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
			}
		}
	}

	private void moveFluids() {
		short newTimeSlot = (short) (container.getWorldObj().getTotalWorldTime() % travelDelay);
		short outputCount = computeCurrentConnectionStatesAndTickFlows(newTimeSlot > 0 && newTimeSlot < travelDelay ? newTimeSlot : 0);

		moveFromPipe(outputCount);
		moveFromCenter();
		moveToCenter();
	}

	private void moveFromPipe(short outputCount) {
		// Move liquid from the non-center to the connected output blocks
		boolean pushed = false;
		if (outputCount > 0) {
			for (ForgeDirection o : directions) {
				if (transferState[o.ordinal()] == TransferState.Output) {
					TileEntity target = this.container.getTile(o);
					if (!(target instanceof IFluidHandler)) {
						continue;
					}

					PipeSection section = sections[o.ordinal()];
					FluidStack liquidToPush = new FluidStack(fluidType, section.drain(flowRate, false));

					if (liquidToPush.amount > 0) {
						int filled = ((IFluidHandler) target).fill(o.getOpposite(), liquidToPush, true);
						section.drain(filled, true);
						pushed = true;
						if (filled <= 0) {
							outputTTL[o.ordinal()]--;
						}
					}
				}
			}
		}

		if (pushed) {
			boolean hasFluid = false;
			for (PipeSection s: sections) {
				if (s.amount > 0) {
					hasFluid = true;
					break;
				}
			}
			if (!hasFluid) {
				fluidType = null;
			}
		}
	}

	private void moveFromCenter() {
		// Split liquids moving to output equally based on flowrate, how much each side can accept and available liquid
		int pushAmount = sections[6].amount;
		int totalAvailable = sections[6].getAvailable();
		if (totalAvailable < 1 || pushAmount < 1) {
			return;
		}

		int testAmount = flowRate;
		// Move liquid from the center to the output sides
		Multiset<ForgeDirection> realDirections = HashMultiset.create(6);
		for (ForgeDirection direction : directions) {
			if (transferState[direction.ordinal()] == TransferState.Output) {
				realDirections.add(direction);
			}
		}

		container.pipe.eventBus.handleEvent(PipeEventFluid.FindDest.class, new PipeEventFluid.FindDest(container.pipe, new FluidStack(fluidType, pushAmount), realDirections));
		float min = Math.min(flowRate, totalAvailable) / (float) flowRate / realDirections.size();

		for (ForgeDirection direction : realDirections.elementSet()) {
			int available = sections[direction.ordinal()].fill(testAmount, false);
			int amountToPush = (int) (available * min * realDirections.count(direction));
			if (amountToPush < 1) {
				amountToPush++;
			}

			amountToPush = sections[6].drain(amountToPush, false);
			if (amountToPush > 0) {
				int filled = sections[direction.ordinal()].fill(amountToPush, true);
				sections[6].drain(filled, true);
			}
		}
	}

	private void moveToCenter() {
		int transferInCount = 0;
		int amountInCenter = sections[6].drain(flowRate, false);
		int spaceAvailable = capacity - amountInCenter;

		for (ForgeDirection dir : directions) {
			inputPerTick[dir.ordinal()] = 0;
			if (transferState[dir.ordinal()] != TransferState.Output) {
				inputPerTick[dir.ordinal()] = sections[dir.ordinal()].drain(flowRate, false);
				transferInCount++;
			}
		}

		float min = Math.min(flowRate, spaceAvailable) / (float) flowRate / transferInCount;
		for (ForgeDirection dir : directions) {
			// Move liquid from input sides to the center
			if (transferState[dir.ordinal()] != TransferState.Output && inputPerTick[dir.ordinal()] > 0) {

				int amountToDrain = (int) (inputPerTick[dir.ordinal()] * min);
				if (amountToDrain < 1) {
					amountToDrain++;
				}

				int amountToPush = sections[dir.ordinal()].drain(amountToDrain, false);
				if (amountToPush > 0) {
					int filled = sections[6].fill(amountToPush, true);
					sections[dir.ordinal()].drain(filled, true);
				}
			}
		}
	}

	private short computeCurrentConnectionStatesAndTickFlows(short newTimeSlot) {
		short outputCount = 0;

		// Processes all interna4al tanks
		for (ForgeDirection direction : orientations) {
			sections[direction.ordinal()].setTime(newTimeSlot);
			sections[direction.ordinal()].moveFluids();
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
			if (canReceiveCache[direction.ordinal()] && outputOpen(direction)) {
				transferState[direction.ordinal()] = TransferState.Output;
				outputCount++;
			}
		}
		return outputCount;
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
		BitSet delta = new BitSet(8);

		if (initClient > 0) {
			initClient--;
			if (initClient == 1) {
				changed = true;
				delta.set(0, 8);
			}
		}

		FluidRenderData renderCacheCopy = this.renderCache;

		if ((fluidType == null && renderCacheCopy.fluidID != 0)
				|| (fluidType != null && renderCacheCopy.fluidID != fluidType.getFluid().getID())) {
			renderCache.fluidID = fluidType != null ? fluidType.getFluid().getID() : 0;
			renderCache.color = fluidType != null ? fluidType.getFluid().getColor(fluidType) : 0;
			delta.set(0);
		}

		for (ForgeDirection dir : orientations) {
			int pamount = renderCache.amount[dir.ordinal()];
			int camount = sections[dir.ordinal()].amount;
			int displayQty = (pamount * 4 + camount) / 5;
			if (displayQty == 0 && camount > 0 || initPacket) {
				displayQty = camount;
			}
			displayQty = Math.min(capacity, displayQty);

			if (pamount != displayQty || initPacket) {
				changed = true;
				renderCache.amount[dir.ordinal()] = displayQty;
				delta.set(dir.ordinal() + 1);
			}
		}

		if (persistChange) {
			this.renderCache = renderCacheCopy;
		}

		if (changed || initPacket) {
			PacketFluidUpdate packet = new PacketFluidUpdate(container.xCoord, container.yCoord, container.zCoord, initPacket);
			packet.renderCache = renderCacheCopy;
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

	public FluidStack getStack(ForgeDirection direction) {
		if (fluidType == null) {
			return null;
		} else {
			return new FluidStack(fluidType, sections[direction.ordinal()].amount);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("fluid")) {
			fluidType = FluidStack.loadFluidStackFromNBT(nbttagcompound.getCompoundTag("fluid"));
		} else {
			fluidType = null;
		}

		for (ForgeDirection direction : orientations) {
			if (nbttagcompound.hasKey("tank[" + direction.ordinal() + "]")) {
				NBTTagCompound compound = nbttagcompound.getCompoundTag("tank[" + direction.ordinal() + "]");
				if (compound.hasKey("FluidType")) {
					FluidStack stack = FluidStack.loadFluidStackFromNBT(compound);
					if (fluidType == null) {
						fluidType = stack;
					}
					if (stack.isFluidEqual(fluidType)) {
						sections[direction.ordinal()].readFromNBT(nbttagcompound);
					}
				} else {
					sections[direction.ordinal()].readFromNBT(nbttagcompound);
				}
			}
			if (direction != ForgeDirection.UNKNOWN) {
				transferState[direction.ordinal()] = TransferState.values()[nbttagcompound.getShort("transferState[" + direction.ordinal() + "]")];
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		NBTTagCompound fluidTag = new NBTTagCompound();
		if (fluidType != null) {
			fluidType.writeToNBT(fluidTag);
			nbttagcompound.setTag("fluid", fluidTag);
		}

		for (ForgeDirection direction : orientations) {
			NBTTagCompound subTag = new NBTTagCompound();
			sections[direction.ordinal()].writeToNBT(subTag);
			nbttagcompound.setTag("tank[" + direction.ordinal() + "]", subTag);
			if (direction != ForgeDirection.UNKNOWN) {
				nbttagcompound.setShort("transferState[" + direction.ordinal() + "]", (short) transferState[direction.ordinal()].ordinal());
			}
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (from != ForgeDirection.UNKNOWN && !inputOpen(from)) {
			return 0;
		}

		if (resource == null || (fluidType != null && !resource.isFluidEqual(fluidType))) {
			return 0;
		}

		int filled;

		if (this.container.pipe instanceof IPipeTransportFluidsHook) {
			filled = ((IPipeTransportFluidsHook) this.container.pipe).fill(from, resource, doFill);
		} else {
			filled = sections[from.ordinal()].fill(resource.amount, doFill);
		}

		if (doFill && filled > 0) {
			if (fluidType == null) {
				fluidType = new FluidStack(resource, 0);
			}
			if (from != ForgeDirection.UNKNOWN) {
				transferState[from.ordinal()] = TransferState.Input;
				inputTTL[from.ordinal()] = INPUT_TTL;
			}
		}

		return filled;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return inputOpen(from);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{new FluidTankInfo(fluidType, sections[from.ordinal()].amount)};
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		for (ForgeDirection direction : directions) {
			if (!container.isPipeConnected(direction)) {
				sections[direction.ordinal()].reset();
				transferState[direction.ordinal()] = TransferState.None;
				renderCache.amount[direction.ordinal()] = 0;
				canReceiveCache[direction.ordinal()] = false;
			} else {
				canReceiveCache[direction.ordinal()] = canReceiveFluid(direction);
			}
		}
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof IPipeTile) {
			Pipe<?> pipe2 = (Pipe<?>) ((IPipeTile) tile).getPipe();
			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportFluids)) {
				return false;
			}
		}

		if (tile instanceof IFluidHandler) {
			return true;
		}

		return tile instanceof IPipeTile;
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
