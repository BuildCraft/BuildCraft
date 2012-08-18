/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.liquids.ILiquidTank;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.liquids.LiquidTank;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.core.IMachine;
import buildcraft.core.ProxyCore;
import buildcraft.core.Utils;
import buildcraft.transport.network.PacketLiquidUpdate;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;

public class PipeTransportLiquids extends PipeTransport implements ITankContainer {


	public class PipeSection extends LiquidTank {

		private short currentTime = 0;

		//Tracks how much of the liquid is inbound in timeslots
		private short[] incomming = new short[travelDelay];

		//Tracks how much is currently available (has spent it's inbound delaytime)

		public PipeSection() {
			super(null, PipeTransportLiquids.LIQUID_IN_PIPE);
		}

		@Override
		public int fill(LiquidStack resource, boolean doFill) {
            if(resource == null)
                return 0;

			int maxToFill = Math.min(resource.amount, flowRate - incomming[currentTime]);
			if (maxToFill <= 0) return 0;

			LiquidStack stackToFill = resource.copy();
			stackToFill.amount = maxToFill;
			int filled = super.fill(stackToFill, doFill);

			if (doFill) {
				incomming[currentTime] += filled;
			}

			return filled;
		}

		@Override
		public LiquidStack drain(int maxDrain, boolean doDrain) {
			int maxToDrain = Math.min(maxDrain, Math.min(flowRate, getAvailable()));
			if (maxToDrain < 0) return null;

			LiquidStack drained = super.drain(maxToDrain, doDrain);
			if (drained == null) return null;

			return drained;
		}

		public void moveLiquids() {
			//Processes the inbound liquid
			incomming[currentTime] = 0;
		}

		public void setTime(short newTime){
			currentTime = newTime;
		}

		public void reset(){
			this.setLiquid(null);
			incomming = new short[travelDelay];

		}

		public int getAvailable(){
			int all = this.getLiquid() != null ? this.getLiquid().amount : 0;
			for(short slot : incomming){
				all -= slot;
			}
			return all;
		}

		public void readFromNBT(NBTTagCompound compoundTag) {
			this.setCapacity(compoundTag.getInteger("capacity"));

			for (int i = 0; i < travelDelay; ++i) {
				incomming[i] = compoundTag.getShort("in[" + i + "]");
			}
			setLiquid(LiquidStack.loadLiquidStackFromNBT(compoundTag));
		}

		public void writeToNBT(NBTTagCompound subTag) {
			subTag.setInteger("capacity", this.getCapacity());

			for (int i = 0; i < travelDelay; ++i) {
				incomming[i] = subTag.getShort("in[" + i + "]");
			}

			if (this.getLiquid() != null){
				this.getLiquid().writeToNBT(subTag);
			}

		}
	}

	public enum TransferState {
		None, Input, Output
	}


	/**
	 * The amount of liquid contained by a pipe section. For simplicity, all
	 * pipe sections are assumed to be of the same volume.
	 */
	public static int LIQUID_IN_PIPE = BuildCraftAPI.BUCKET_VOLUME / 4;
	public static short INPUT_TTL = 60;	//100
	public static short OUTPUT_TTL = 80;	//80
	public static short OUTPUT_COOLDOWN = 30;	//30


	public short travelDelay = 12;
	public short flowRate = 20;
	public LiquidStack[] renderCache = new LiquidStack[Orientations.values().length];

	private final PipeSection[] internalTanks = new PipeSection[Orientations.values().length];

	private final TransferState[] transferState = new TransferState[Orientations.dirs().length];

	private final short[] inputTTL = new short[] { 0, 0, 0, 0, 0, 0 };
	private final short[] outputTTL = new short[] { OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL, OUTPUT_TTL };
	private final short[] outputCooldown = new short[] {0, 0, 0, 0, 0, 0 };

	private final SafeTimeTracker tracker = new SafeTimeTracker();


	public PipeTransportLiquids() {
		for (Orientations direction : Orientations.values()) {
			internalTanks[direction.ordinal()] = new PipeSection();
			if (direction != Orientations.Unknown){
				transferState[direction.ordinal()] = TransferState.None;
			}
		}
	}

	public boolean canReceiveLiquid(Orientations o) {
		TileEntity entity = container.getTile(o);

		if (!Utils.checkPipesConnections(container, entity))
			return false;

		if (entity instanceof TileGenericPipe){
			if (!((TileGenericPipe)entity).pipe.inputOpen(o.reverse())){
				return false;
			}
		}

		if (entity instanceof IPipeEntry || entity instanceof ITankContainer)
			return true;

		return false;
	}

	@Override
	public void updateEntity() {
		if (ProxyCore.proxy.isRemote(worldObj))
			return;

		moveLiquids();
		for (Orientations direction : Orientations.values()) {
			LiquidStack liquid = internalTanks[direction.ordinal()].getLiquid();

			if (liquid != null){
				if (renderCache[direction.ordinal()] == null){
					renderCache[direction.ordinal()] = liquid.copy();
				} else {
					renderCache[direction.ordinal()].itemID = liquid.itemID;
					renderCache[direction.ordinal()].itemMeta = liquid.itemMeta;
				}
			}

			if (renderCache[direction.ordinal()] != null){
				int currentLiquid = liquid != null ? liquid.amount : 0;
				renderCache[direction.ordinal()].amount = (short) Math.min(LIQUID_IN_PIPE, ((renderCache[direction.ordinal()].amount * 9 + currentLiquid) / 10));
				if (renderCache[direction.ordinal()].amount == 0 && currentLiquid > 0){
					renderCache[direction.ordinal()].amount = currentLiquid;
				}

				//Uncomment to disable the avaraging
				//renderCache[direction.ordinal()].amount = (liquid != null ? liquid.amount : 0);
			}

			//Uncomment to disable the renderstate and show actual values
//			renderCache[direction.ordinal()] = internalTanks[direction.ordinal()].getLiquid();
//			if (renderCache[direction.ordinal()] != null){
//				renderCache[direction.ordinal()] = renderCache[direction.ordinal()].copy();
//			}
		}


		if (ProxyCore.proxy.isSimulating(worldObj))
			if (tracker.markTimeIfDelay(worldObj, 1 * BuildCraftCore.updateFactor)){

				PacketLiquidUpdate packet = new PacketLiquidUpdate(xCoord, yCoord, zCoord);
				packet.displayLiquid = this.renderCache;
//				CoreProxy.sendToPlayers(packet.getPacket(), worldObj, xCoord, yCoord, zCoord,
//						DefaultProps.NETWORK_UPDATE_RANGE, mod_BuildCraftCore.instance);
			}

		//this.container.synchronizeIfDelay(1 * BuildCraftCore.updateFactor);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (Orientations direction : Orientations.values()) {
			if (nbttagcompound.hasKey("tank[" + direction.ordinal() + "]")){
				internalTanks[direction.ordinal()].readFromNBT(nbttagcompound.getCompoundTag("tank[" + direction.ordinal() + "]"));
			}
			if (direction != Orientations.Unknown){
				transferState[direction.ordinal()] = TransferState.values()[nbttagcompound.getShort("transferState[" + direction.ordinal() + "]")];
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (Orientations direction : Orientations.values()) {
			NBTTagCompound subTag = new NBTTagCompound();
			internalTanks[direction.ordinal()].writeToNBT(subTag);
			nbttagcompound.setTag("tank[" + direction.ordinal() + "]", subTag);
			if (direction != Orientations.Unknown){
				nbttagcompound.setShort("transferState[" + direction.ordinal() + "]", (short)transferState[direction.ordinal()].ordinal());
			}
		}
	}

	private void moveLiquids() {
		short newTimeSlot =  (short) (worldObj.getWorldTime() % travelDelay);

		//Processes all internal tanks
		for (Orientations direction : Orientations.values()){
			internalTanks[direction.ordinal()].setTime(newTimeSlot);
			internalTanks[direction.ordinal()].moveLiquids();

		}

		for (Orientations direction : Orientations.dirs()){
			if (transferState[direction.ordinal()] == TransferState.Input){
				if ((inputTTL[direction.ordinal()]--) < 1){
					transferState[direction.ordinal()] = TransferState.None;
				}
			}
		}

		short outputCount = computeOutputs();
		moveFromPipe(outputCount);
		moveFromCenter();
		moveToCenter();
	}

	private void moveFromPipe(short outputCount) {
		//Move liquid from the non-center to the connected output blocks
		if (outputCount > 0){
			for (Orientations o : Orientations.dirs()){
				if (transferState[o.ordinal()] == TransferState.Output){
					TileEntity target = this.container.getTile(o);
					if (!(target instanceof ITankContainer)) continue;

					LiquidStack liquidToPush = internalTanks[o.ordinal()].drain(flowRate, false);
					if (liquidToPush != null && liquidToPush.amount > 0) {
						int filled = ((ITankContainer)target).fill(o.reverse(), liquidToPush, true);
						internalTanks[o.ordinal()].drain(filled, true);
						if (filled <= 0){
							outputTTL[o.ordinal()]--;
						}
					}
				}
			}
		}
	}

	private void moveFromCenter() {
		//Split liquids moving to output equally based on flowrate, how much each side can accept and available liquid
		int[] maxOutput = new int[] {0,0,0,0,0,0};
		int transferOutCount = 0;
		LiquidStack pushStack = internalTanks[Orientations.Unknown.ordinal()].getLiquid();
		int totalAvailable = internalTanks[Orientations.Unknown.ordinal()].getAvailable();
		if (totalAvailable < 1) return;
		if (pushStack != null){
			LiquidStack testStack = pushStack.copy();
			testStack.amount = flowRate;
			for (Orientations direction : Orientations.dirs()){
				if (transferState[direction.ordinal()] != TransferState.Output ) continue;

				maxOutput[direction.ordinal()] = internalTanks[direction.ordinal()].fill(testStack, false);
				if(maxOutput[direction.ordinal()] > 0){
					transferOutCount++;
				}
			}
			if (transferOutCount <= 0) return;
			//Move liquid from the center to the output sides
			for (Orientations direction : Orientations.dirs()) {
				if (transferState[direction.ordinal()] == TransferState.Output)	{
					if (maxOutput[direction.ordinal()] == 0) continue;
					int ammountToPush = (int) ((double) maxOutput[direction.ordinal()] / (double) flowRate / (double) transferOutCount * (double) Math.min(flowRate, totalAvailable));
					if (ammountToPush < 1) ammountToPush++;

					LiquidStack liquidToPush = internalTanks[Orientations.Unknown.ordinal()].drain(ammountToPush, false);
					if (liquidToPush != null) {
						int filled = internalTanks[direction.ordinal()].fill(liquidToPush, true);
						internalTanks[Orientations.Unknown.ordinal()].drain(filled, true);
					}
				}
			}
		}
	}

	private void moveToCenter() {
		int [] maxInput = new int[] {0,0,0,0,0,0};
		int transferInCount = 0;
		LiquidStack stackInCenter = internalTanks[Orientations.Unknown.ordinal()].drain(flowRate, false);
		int spaceAvailable = internalTanks[Orientations.Unknown.ordinal()].getCapacity();
		if (stackInCenter != null){
			spaceAvailable -= stackInCenter.amount;
		}


		for (Orientations direction : Orientations.dirs()){
			LiquidStack testStack = internalTanks[direction.ordinal()].drain(flowRate, false);
			if (testStack == null) continue;
			if (stackInCenter != null && !stackInCenter.isLiquidEqual(testStack)) continue;
			maxInput[direction.ordinal()] = testStack.amount;
			transferInCount++;
		}

		for (Orientations direction : Orientations.dirs()){
			//Move liquid from input sides to the center
			if (transferState[direction.ordinal()] != TransferState.Output && maxInput[direction.ordinal()] > 0){

				int ammountToDrain = (int) ((double) maxInput[direction.ordinal()] / (double) flowRate / (double) transferInCount * (double) Math.min(flowRate, spaceAvailable));
				if (ammountToDrain < 1){
					ammountToDrain++;
				}

				LiquidStack liquidToPush = internalTanks[direction.ordinal()].drain(ammountToDrain, false);
				if (liquidToPush != null) {
					int filled = internalTanks[Orientations.Unknown.ordinal()].fill(liquidToPush, true);
					internalTanks[direction.ordinal()].drain(filled, true);
				}
			}
		}
	}

	private short computeOutputs() {
		short outputCount = 0;

		for (Orientations o : Orientations.dirs()) {
			if (transferState[o.ordinal()] == TransferState.Input) continue;
			if (!container.pipe.outputOpen(o)){
				transferState[o.ordinal()] = TransferState.None;
				continue;
			}
			if (outputCooldown[o.ordinal()] > 0){
				outputCooldown[o.ordinal()]--;

				continue;
			}
			if (outputTTL[o.ordinal()] <= 0){
				transferState[o.ordinal()] = TransferState.None;
				outputCooldown[o.ordinal()] = OUTPUT_COOLDOWN;
				outputTTL[o.ordinal()] = OUTPUT_TTL;
				continue;
			}

			if (canReceiveLiquid(o)) {
				transferState[o.ordinal()] = TransferState.Output;
				outputCount++;
			}
		}

		return outputCount;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		for (Orientations direction : Orientations.dirs()){
			if (!Utils.checkPipesConnections(container.getTile(Orientations.values()[direction.ordinal()]), container)) {
				internalTanks[direction.ordinal()].reset();
				transferState[direction.ordinal()] = TransferState.None;
				renderCache[direction.ordinal()] = null;
			}
		}
	}

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		if (tile instanceof ITankContainer) {
			ITankContainer liq = (ITankContainer) tile;

			if (liq.getTanks() != null && liq.getTanks().length > 0)
				return true;
		}

		return tile instanceof TileGenericPipe || (tile instanceof IMachine && ((IMachine) tile).manageLiquids());
	}

	public boolean isTriggerActive(ITrigger trigger) {
		return false;
	}

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportLiquids;
	}

	public void handleLiquidPacket(PacketLiquidUpdate packetLiquid) {
		this.renderCache = packetLiquid.displayLiquid;

	}

	/** ITankContainer implementation **/

	@Override
	public int fill(Orientations from, LiquidStack resource, boolean doFill) {
		return fill(from.ordinal(), resource, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		int filled;

		if (this.container.pipe instanceof IPipeTransportLiquidsHook)
			filled = ((IPipeTransportLiquidsHook) this.container.pipe).fill(Orientations.values()[tankIndex], resource, doFill);
		else
			filled = internalTanks[tankIndex].fill(resource, doFill);

		if (filled > 0 && doFill && tankIndex != Orientations.Unknown.ordinal()){
			transferState[tankIndex] = TransferState.Input;
			inputTTL[tankIndex] = INPUT_TTL;
		}
		return filled;
	}

	@Override
	public LiquidStack drain(Orientations from, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		return null;
	}

	@Override
	public ILiquidTank[] getTanks() {
		return internalTanks;
	}

}
