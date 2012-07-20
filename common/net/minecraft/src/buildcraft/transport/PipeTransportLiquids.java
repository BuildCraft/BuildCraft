/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityItem;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.api.liquids.ILiquidTank;
import net.minecraft.src.buildcraft.api.liquids.ITankContainer;
import net.minecraft.src.buildcraft.api.liquids.LiquidStack;
import net.minecraft.src.buildcraft.api.liquids.LiquidTank;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.Utils;

public class PipeTransportLiquids extends PipeTransport implements ITankContainer {

	/**
	 * The amount of liquid contained by a pipe section. For simplicity, all
	 * pipe sections are assumed to be of the same volume.
	 */
	public static int LIQUID_IN_PIPE = BuildCraftAPI.BUCKET_VOLUME / 4;

	public short travelDelay = 8;
	public short flowRate = 20;
	
	private final PipeSection[] internalTanks = new PipeSection[Orientations.values().length];
	
	public class PipeSection extends LiquidTank {
		
		private short currentTime = 0;
		
		//Tracks how much of the liquid is inbound in timeslots
		private short[] incomming = new short[travelDelay];

		//Tracks how much is currently available (has spent it's inbound delaytime)
		private short available;
		
		public PipeSection() {
			super(null, PipeTransportLiquids.LIQUID_IN_PIPE);
		}
		
		@Override
		public int fill(LiquidStack resource, boolean doFill) {
			int interalAmmount = this.getLiquid() != null ? this.getLiquid().amount : 0; 
			int maxToFill = Math.min(resource.amount, Math.min(flowRate - incomming[currentTime], this.getCapacity() - interalAmmount ));
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
			int maxToDrain = Math.min(maxDrain, Math.min(flowRate, available));
			if (maxToDrain < 0) return null;
			
			LiquidStack drained = super.drain(maxToDrain, doDrain);
			if (drained == null) return null;
			
			if (doDrain){
				available -= drained.amount;
			}
			
			return drained;
		}
		
		public void moveLiquids() {
			//Processes the inbound liquid
			available += incomming[currentTime];
			incomming[currentTime] = 0;
		}
		
		public void setTime(short newTime){
			currentTime = newTime;
		}
		
		public void reset(){
			this.setLiquid(null);
			available = 0;
			incomming = new short[travelDelay];
			
		}
		
		public int getAvailable(){
			return available;
		}
	}

	public class LiquidBuffer {
		short[] in = new short[travelDelay];
		short ready;
		short[] out = new short[travelDelay];
		short qty;
		int orientation;

		short[] lastQty = new short[100];
		int lastTotal = 0;

		int emptyTime = 0;

		@TileNetworkData(intKind = TileNetworkData.UNSIGNED_BYTE)
		public int average;
		@TileNetworkData
		public int liquidId = 0;

		int totalBounced = 0;
		boolean bouncing = false;

		private boolean[] filled;

		public LiquidBuffer(int o) {
			this.orientation = o;

			reset();
		}

		public void reset() {
			for (int i = 0; i < travelDelay; ++i) {
				in[i] = 0;
				out[i] = 0;
			}

			for (int i = 0; i < lastQty.length; ++i)
				lastQty[i] = 0;

			ready = 0;
			qty = 0;
			liquidId = 0;
			lastTotal = 0;
			totalBounced = 0;
			emptyTime = 0;
		}

		public int fill(int toFill, boolean doFill, int liquidId) {
			if (worldObj == null)
				return 0;

			if (qty > 0 && this.liquidId != liquidId && this.liquidId != 0)
				return 0;

			if (this.liquidId != liquidId)
				reset();

			this.liquidId = liquidId;

			int date = (int) (worldObj.getWorldTime() % travelDelay);
			int newDate = date > 0 ? date - 1 : travelDelay - 1;

			if (qty + toFill > LIQUID_IN_PIPE + flowRate)
				toFill = LIQUID_IN_PIPE + flowRate - qty;

			if (doFill) {
				qty += toFill;
				in[newDate] += toFill;
			}

			return toFill;
		}

		public int empty(int toEmpty) {
			int date = (int) (worldObj.getWorldTime() % travelDelay);
			int newDate = date > 0 ? date - 1 : travelDelay - 1;

			if (ready - toEmpty < 0)
				toEmpty = ready;

			ready -= toEmpty;

			out[newDate] += toEmpty;

			return toEmpty;
		}

		public void update() {
//			bouncing = false;
//
//			int date = (int) (worldObj.getWorldTime() % travelDelay);
//
//			ready += in[date];
//			in[date] = 0;
//
//			if (out[date] != 0) {
//				int extracted = 0;
//
//				if (orientation < 6) {
//					if (isInput[orientation])
//						extracted = center.fill(out[date], true, liquidId);
//					if (isOutput[orientation]) {
//						Position p = new Position(xCoord, yCoord, zCoord, Orientations.values()[orientation]);
//						p.moveForwards(1);
//
//						ITankContainer nextPipe = (ITankContainer) container.getTile(Orientations.values()[orientation]);
//						extracted = nextPipe.fill(p.orientation.reverse(), new LiquidStack(out[date], liquidId), true);
//
//						if (extracted == 0) {
//							totalBounced++;
//
//							if (totalBounced > 20)
//								bouncing = true;
//
//							extracted += center.fill(out[date], true, liquidId);
//						} else
//							totalBounced = 0;
//					}
//				} else {
//					int outputNumber = 0;
//
//					for (int i = 0; i < 6; ++i)
//						if (isOutput[i])
//							outputNumber++;
//
//					filled = new boolean[] { false, false, false, false, false, false };
//
//					// try first, to detect filled outputs
//					extracted = splitLiquid(out[date], outputNumber);
//
//					if (extracted < out[date]) {
//						outputNumber = 0;
//
//						// try a second time, if to split the remaining in non
//						// filled if any
//						for (int i = 0; i < 6; ++i)
//							if (isOutput[i] && !filled[i])
//								outputNumber++;
//
//						extracted += splitLiquid(out[date] - extracted, outputNumber);
//					}
//				}
//
//				qty -= extracted;
//				ready += out[date] - extracted;
//				out[date] = 0;
//			}
//
//			int avgDate = (int) (worldObj.getWorldTime() % lastQty.length);
//
//			lastTotal += qty - lastQty[avgDate];
//			lastQty[avgDate] = qty;
//
//			average = lastTotal / lastQty.length;
//
//			if (qty != 0 && average == 0)
//				average = 1;
		}

//		private int splitLiquid(int quantity, int outputNumber) {
//			int extracted = 0;
//
//			int slotExtract = (int) Math.ceil(((double) quantity / (double) outputNumber));
//
//			int[] splitVector = getSplitVector(worldObj);
//
//			for (int r = 0; r < 6; ++r) {
//				int toExtract = slotExtract <= quantity ? slotExtract : quantity;
//
//				int i = splitVector[r];
//
//				if (isOutput[i] && !filled[i]) {
//					extracted += side[i].fill(toExtract, true, liquidId);
//					quantity -= toExtract;
//
//					if (extracted != toExtract)
//						filled[i] = true;
//				}
//			}
//
//			return extracted;
//		}

//		public void readFromNBT(NBTTagCompound nbttagcompound) {
//			for (int i = 0; i < travelDelay; ++i) {
//				in[i] = nbttagcompound.getShort("in[" + i + "]");
//				out[i] = nbttagcompound.getShort("out[" + i + "]");
//			}
//
//			ready = nbttagcompound.getShort("ready");
//			qty = nbttagcompound.getShort("qty");
//			liquidId = nbttagcompound.getInteger("liquidId");
//		}
//
//		public void writeToNBT(NBTTagCompound nbttagcompound) {
//			for (int i = 0; i < travelDelay; ++i) {
//				nbttagcompound.setShort("in[" + i + "]", in[i]);
//				nbttagcompound.setShort("out[" + i + "]", out[i]);
//			}
//
//			nbttagcompound.setShort("ready", ready);
//			nbttagcompound.setShort("qty", qty);
//			nbttagcompound.setInteger("liquidId", liquidId);
//		}

	}

//	public @TileNetworkData(staticSize = 6)
//	LiquidBuffer[] side = new LiquidBuffer[6];
//	public @TileNetworkData
//	LiquidBuffer center;

	boolean[] isInput = new boolean[] { false, false, false, false, false, false, false };

	// Computed at each update
	boolean isOutput[] = new boolean[] { false, false, false, false, false, false, false };

	public PipeTransportLiquids() {
		for (Orientations direction : Orientations.values()) {
			internalTanks[direction.ordinal()] = new PipeSection();
			isInput[direction.ordinal()] = false;
		}
	}

	public boolean canReceiveLiquid(Orientations o) {
		TileEntity entity = container.getTile(o);

		if (isInput[o.ordinal()])
			return false;

		if (!Utils.checkPipesConnections(container, entity))
			return false;

		if (entity instanceof IPipeEntry || entity instanceof ITankContainer)
			return true;

		return false;
	}

	@Override
	public void updateEntity() {
		if (APIProxy.isClient(worldObj))
			return;

		moveLiquids();

		this.container.synchronizeIfDelay(1 * BuildCraftCore.updateFactor);
	}

//	@Override
//	public void readFromNBT(NBTTagCompound nbttagcompound) {
//		super.readFromNBT(nbttagcompound);
//
//		for (int i = 0; i < 6; ++i) {
//			if (nbttagcompound.hasKey("side[" + i + "]"))
//				side[i].readFromNBT(nbttagcompound.getCompoundTag("side[" + i + "]"));
//
//			isInput[i] = nbttagcompound.getBoolean("isInput[" + i + "]");
//		}
//
//		if (nbttagcompound.hasKey("center"))
//			center.readFromNBT(nbttagcompound.getCompoundTag("center"));
//
//		NBTTagCompound sub = new NBTTagCompound();
//		center.writeToNBT(sub);
//		nbttagcompound.setTag("center", sub);
//	}

//	@Override
//	public void writeToNBT(NBTTagCompound nbttagcompound) {
//		super.writeToNBT(nbttagcompound);
//
//		for (int i = 0; i < 6; ++i) {
//			NBTTagCompound sub = new NBTTagCompound();
//			side[i].writeToNBT(sub);
//			nbttagcompound.setTag("side[" + i + "]", sub);
//
//			nbttagcompound.setBoolean("isInput[" + i + "]", isInput[i]);
//		}
//
//		NBTTagCompound sub = new NBTTagCompound();
//		center.writeToNBT(sub);
//		nbttagcompound.setTag("center", sub);
//	}

	public void onDropped(EntityItem item) {

	}

	int lockedTime = 0;

	private void moveLiquids() {
		short newTimeSlot =  (short) (worldObj.getWorldTime() % travelDelay);
		
		//Processes all internal tanks
		for (Orientations direction : Orientations.values()){
			internalTanks[direction.ordinal()].setTime(newTimeSlot);
			internalTanks[direction.ordinal()].moveLiquids();
		}
		
		
		short outputCount = computeOutputs();

		if (outputCount == 0) {
			lockedTime++;
		} else {
			lockedTime = 0;
		}

		//If enough time has passed, previous input sides are now valid destinations
		if (lockedTime > 20) {
			for (int i = 0; i < 6; ++i) {
				isInput[i] = false;
			}
			outputCount = computeOutputs();
		}
		
		
		int [] maxInput = new int[] {0,0,0,0,0,0};
		int transferInCount = 0;
		LiquidStack stackInCenter = internalTanks[Orientations.Unknown.ordinal()].drain(flowRate, false);
		int spaceAvailable = internalTanks[Orientations.Unknown.ordinal()].getCapacity();
		if (stackInCenter != null){
			spaceAvailable -= stackInCenter.amount;
		}
		
		
		for (Orientations direction : Orientations.dirs()){
			if (!container.pipe.outputOpen(direction)) continue;
			LiquidStack testStack = internalTanks[direction.ordinal()].drain(flowRate, false);
			if (testStack == null) continue;
			if (stackInCenter != null && !stackInCenter.isLiquidEqual(testStack)) continue;
			maxInput[direction.ordinal()] = testStack.amount;
			transferInCount++;
		}
				
		for (Orientations direction : Orientations.dirs()){
			//Move liquid from non output sides to the center
			if (!isOutput[direction.ordinal()] && maxInput[direction.ordinal()] > 0){
				
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

		
		//Split liquids moving to output equally based on flowrate, how much each side can accept and available liquid
		int[] maxOutput = new int[] {0,0,0,0,0,0};
		int transferOutCount = 0;
		LiquidStack pushStack = internalTanks[Orientations.Unknown.ordinal()].getLiquid();
		int totalAvailable = internalTanks[Orientations.Unknown.ordinal()].getAvailable();
		if (pushStack != null){
			LiquidStack testStack = pushStack.copy();
			testStack.amount = flowRate;
			for (Orientations direction : Orientations.dirs()){
				if (!isOutput[direction.ordinal()]) continue;
			
				maxOutput[direction.ordinal()] = internalTanks[direction.ordinal()].fill(testStack, false);
				if(maxOutput[direction.ordinal()] > 0){
					transferOutCount++;
				}
			}
			//Move liquid from the center to the output sides
			for (Orientations direction : Orientations.dirs()) {
				if (!container.pipe.outputOpen(direction)) continue;
				if (isOutput[direction.ordinal()])	{
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
		
		//Move liquid from the non-center to the connected output blocks
		if (outputCount > 0){
			for (Orientations o : Orientations.dirs()){
				if (isOutput[o.ordinal()]){
					TileEntity target = this.container.getTile(o);
					if (!(target instanceof ITankContainer)) continue;
					
					LiquidStack liquidToPush = internalTanks[o.ordinal()].drain(flowRate, false);
					if (liquidToPush != null) {
						int filled = ((ITankContainer)target).fill(o.reverse(), liquidToPush, true);
						internalTanks[o.ordinal()].drain(filled, true);
					}
				}
			}
		}
		

//		int[] rndIt = getSplitVector(worldObj);
//
//		for (int r = 0; r < 6; ++r) {
//			int i = rndIt[r];
//
//			side[i].empty(flowRate);
//		}
//
//		center.empty(flowRate);
//
//		// APPLY SCHEDULED FILLED ORDERS
//
//		center.update();
//
//		for (int r = 0; r < 6; ++r) {
//			int i = rndIt[r];
//
//			side[i].update();
//
//			if (side[i].qty != 0)
//				side[i].emptyTime = 0;
//
//			if (side[i].bouncing)
//				isInput[i] = true;
//			else if (side[i].qty == 0)
//				side[i].emptyTime++;
//
//			if (side[i].emptyTime > 20)
//				isInput[i] = false;
//		}
	}

	private short computeOutputs() {
		short outputCount = 0;

		for (Orientations o : Orientations.dirs()) {
			isOutput[o.ordinal()] = container.pipe.outputOpen(o) && canReceiveLiquid(o) && !isInput[o.ordinal()];
			if (isOutput[o.ordinal()]){
				outputCount++;
			}
		}

		return outputCount;
	}

//	public int getSide(int orientation) {
//		if (side[orientation].average > LIQUID_IN_PIPE)
//			return LIQUID_IN_PIPE;
//		else
//			return side[orientation].average;
//	}
//
//	public int getCenter() {
//		if (center.average > LIQUID_IN_PIPE)
//			return LIQUID_IN_PIPE;
//		else
//			return center.average;
//	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		for (int i = 0; i < 6; ++i){
			if (!Utils.checkPipesConnections(container.getTile(Orientations.values()[i]), container)) {
				internalTanks[i].reset();
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

	private static long lastSplit = -1;

	private static int[] splitVector;

	public static int[] getSplitVector(World worldObj) {
		if (lastSplit == worldObj.getWorldTime())
			return splitVector;

		lastSplit = worldObj.getWorldTime();

		splitVector = new int[6];

		for (int i = 0; i < 6; ++i)
			splitVector[i] = i;

		for (int i = 0; i < 20; ++i) {
			int a = worldObj.rand.nextInt(6);
			int b = worldObj.rand.nextInt(6);

			int tmp = splitVector[a];
			splitVector[a] = splitVector[b];
			splitVector[b] = tmp;
		}

		return splitVector;
	}

	public boolean isTriggerActive(Trigger trigger) {
		return false;
	}

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportLiquids;
	}
	
	/** ITankContainer implementation **/
	
	@Override
	public int fill(Orientations from, LiquidStack resource, boolean doFill) {
		return fill(from.ordinal(), resource, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		isInput[tankIndex] = true;

		if (this.container.pipe instanceof IPipeTransportLiquidsHook)
			return ((IPipeTransportLiquidsHook) this.container.pipe).fill(Orientations.values()[tankIndex], resource, doFill);
		else
			return internalTanks[tankIndex].fill(resource, doFill);
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
