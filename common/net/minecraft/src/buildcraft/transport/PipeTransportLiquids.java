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
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.LiquidSlot;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.Utils;

public class PipeTransportLiquids extends PipeTransport implements ILiquidContainer {

	/**
	 * The amount of liquid contained by a pipe section. For simplicity, all
	 * pipe sections are assumed to be of the same volume.
	 */
	public static int LIQUID_IN_PIPE = BuildCraftAPI.BUCKET_VOLUME / 4;

	public int travelDelay = 6;
	public int flowRate = 20;

	public class LiquidBuffer {
		short [] in = new short [travelDelay];
		short ready;
		short [] out = new short [travelDelay];
		short qty;
		int orientation;

		short [] lastQty = new short [100];
		int lastTotal = 0;

		int emptyTime = 0;

		@TileNetworkData (intKind = TileNetworkData.UNSIGNED_BYTE)
		public int average;
		@TileNetworkData
		public short liquidId = 0;

		int totalBounced = 0;
		boolean bouncing = false;

		private boolean [] filled;

		public LiquidBuffer (int o) {
			this.orientation = o;

			reset ();
		}

		public void reset() {
			for (int i = 0; i < travelDelay; ++i) {
				in [i] = 0;
				out [i] = 0;
			}

			for (int i = 0; i < lastQty.length; ++i)
				lastQty [i] = 0;

			ready = 0;
			qty = 0;
			liquidId = 0;
			lastTotal = 0;
			totalBounced = 0;
			emptyTime = 0;
		}

		public int fill (int toFill, boolean doFill, short liquidId) {
			if (worldObj == null)
				return 0;

			if (qty > 0 && this.liquidId != liquidId && this.liquidId != 0)
				return 0;

			if (this.liquidId != liquidId)
				reset ();

			this.liquidId = liquidId;

			int date = (int) (worldObj.getWorldTime() % travelDelay);
			int newDate = date > 0 ? date - 1 : travelDelay - 1;

			if (qty + toFill > LIQUID_IN_PIPE + flowRate)
				toFill = LIQUID_IN_PIPE + flowRate - qty;

			if (doFill) {
				qty += toFill;
				in [newDate] += toFill;
			}

			return toFill;
		}

		public int empty (int toEmpty) {
			int date = (int) (worldObj.getWorldTime() % travelDelay);
			int newDate = date > 0 ? date - 1 : travelDelay - 1;

			if (ready - toEmpty < 0)
				toEmpty = ready;

			ready -= toEmpty;

			out [newDate] += toEmpty;

			return toEmpty;
		}

		public void update () {
			bouncing = false;

			int date = (int) (worldObj.getWorldTime() % travelDelay);

			ready += in [date];
			in [date] = 0;

			if (out [date] != 0) {
				int extracted = 0;

				if (orientation < 6) {
					if (isInput [orientation])
						extracted = center.fill(out [date], true, liquidId); if (isOutput[orientation]) {
						Position p = new Position(xCoord, yCoord, zCoord,
								Orientations.values()[orientation]);
						p.moveForwards(1);

						ILiquidContainer nextPipe = (ILiquidContainer) container
								.getTile(Orientations.values()[orientation]);
						extracted = nextPipe.fill(p.orientation.reverse(),
								out[date], liquidId, true);

						if (extracted == 0) {
							totalBounced++;

							if (totalBounced > 20)
								bouncing = true;

							extracted += center.fill(out [date], true, liquidId);
						} else
							totalBounced = 0;
					}
				} else {
					int outputNumber = 0;

					for (int i = 0; i < 6; ++i)
						if (isOutput [i])
							outputNumber++;

					filled = new boolean [] {false, false, false, false, false, false};

					// try first, to detect filled outputs
					extracted = splitLiquid(out [date], outputNumber);

					if (extracted < out [date]) {
						outputNumber = 0;

						// try a second time, if to split the remaining in non
						// filled if any
						for (int i = 0; i < 6; ++i)
							if (isOutput [i] && !filled [i])
								outputNumber++;

						extracted += splitLiquid(out [date] - extracted, outputNumber);
					}
				}

				qty -= extracted;
				ready += out[date] - extracted;
				out[date] = 0;
			}

			int avgDate = (int) (worldObj.getWorldTime() % lastQty.length);

			lastTotal += qty - lastQty [avgDate];
			lastQty [avgDate] = qty;

			average = lastTotal / lastQty.length;

			if (qty != 0 && average == 0)
				average = 1;
		}

		private int splitLiquid (int quantity, int outputNumber) {
			int extracted = 0;

			int slotExtract = (int) Math
			.ceil(((double) quantity / (double) outputNumber));

			int [] splitVector = getSplitVector(worldObj);

			for (int r = 0; r < 6; ++r) {
				int toExtract = slotExtract <= quantity ? slotExtract : quantity;

				int i = splitVector [r];

				if (isOutput [i] && !filled [i]) {
					extracted += side [i].fill(toExtract, true, liquidId);
					quantity -= toExtract;

					if (extracted != toExtract)
						filled [i] = true;
				}
			}

			return extracted;
		}

		public void readFromNBT(NBTTagCompound nbttagcompound) {
			for (int i = 0; i < travelDelay; ++i) {
				in [i] = nbttagcompound.getShort("in[" + i + "]");
				out [i] = nbttagcompound.getShort("out[" + i + "]");
			}

			ready = nbttagcompound.getShort("ready");
			qty = nbttagcompound.getShort("qty");
			liquidId = nbttagcompound.getShort("liquidId");
		}

		public void writeToNBT(NBTTagCompound nbttagcompound) {
			for (int i = 0; i < travelDelay; ++i) {
				nbttagcompound.setShort("in[" + i + "]", in [i]);
				nbttagcompound.setShort("out[" + i + "]", out [i]);
			}

			nbttagcompound.setShort("ready", ready);
			nbttagcompound.setShort("qty", qty);
			nbttagcompound.setShort("liquidId", liquidId);
		}


	}

	public @TileNetworkData(staticSize = 6)
	LiquidBuffer[] side = new LiquidBuffer [6];
	public @TileNetworkData
	LiquidBuffer center;

	boolean[] isInput = new boolean[6];

	// Computed at each update
	boolean isOutput [] = new boolean [] {false, false, false, false, false, false};

	public PipeTransportLiquids() {
		for (int j = 0; j < 6; ++j) {
			side[j] = new LiquidBuffer(j);
			isInput[j] = false;
		}

		center = new LiquidBuffer(6);
	}

	public boolean canReceiveLiquid(Orientations o) {
		TileEntity entity = container.getTile(o);

		if (isInput[o.ordinal()])
			return false;

		if (!Utils.checkPipesConnections(container, entity))
			return false;

		if (entity instanceof IPipeEntry || entity instanceof ILiquidContainer)
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

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			if (nbttagcompound.hasKey("side[" + i + "]"))
				side [i].readFromNBT(nbttagcompound.getCompoundTag("side[" + i + "]"));

			isInput [i] = nbttagcompound.getBoolean("isInput[" + i + "]");
		}

		if (nbttagcompound.hasKey("center"))
			center.readFromNBT(nbttagcompound.getCompoundTag("center"));

		NBTTagCompound sub = new NBTTagCompound();
		center.writeToNBT(sub);
		nbttagcompound.setTag("center", sub);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			NBTTagCompound sub = new NBTTagCompound();
			side [i].writeToNBT(sub);
			nbttagcompound.setTag("side[" + i + "]", sub);

			nbttagcompound.setBoolean("isInput[" + i + "]", isInput [i]);
		}

		NBTTagCompound sub = new NBTTagCompound();
		center.writeToNBT(sub);
		nbttagcompound.setTag("center", sub);
	}

	protected void doWork() {
	}

	public void onDropped(EntityItem item) {

	}

	/**
	 * Fills the pipe, and return the amount of liquid that has been used.
	 */
	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		isInput[from.ordinal()] = true;

		if (this.container.pipe instanceof IPipeTransportLiquidsHook)
			return ((IPipeTransportLiquidsHook) this.container.pipe).fill(from,
					quantity, id, doFill);
		else
			return side[from.ordinal()].fill(quantity, doFill, (short) id);
	}

	int lockedTime = 0;

	private void moveLiquids() {
		isOutput = new boolean [] {false, false, false, false, false, false};

		int outputNumber = computeOutputs ();

		if (outputNumber == 0)
			lockedTime++;
		else
			lockedTime = 0;

		if (lockedTime > 20) {
			for (int i = 0; i < 6; ++i)
				isInput[i] = false;

			outputNumber = computeOutputs();
		}


		int [] rndIt = getSplitVector(worldObj);

		for (int r = 0; r < 6; ++r) {
			int i = rndIt [r];

			side [i].empty(flowRate);
		}

		center.empty(flowRate);

		// APPLY SCHEDULED FILLED ORDERS

		center.update();

		for (int r = 0; r < 6; ++r) {
			int i = rndIt [r];

			side [i].update();

			if (side [i].qty != 0)
				side [i].emptyTime = 0;

			if (side [i].bouncing)
				isInput [i] = true;
			else if (side [i].qty == 0)
				side [i].emptyTime++;

			if (side [i].emptyTime > 20)
				isInput [i] = false;
		}
	}

	private int computeOutputs() {
		int outputNumber = 0;

		for (Orientations o : Orientations.dirs()) {
			isOutput [o.ordinal()] = container.pipe.outputOpen(o)
					&& canReceiveLiquid(o) && !isInput[o.ordinal()];

			if (isOutput [o.ordinal()])
				outputNumber++;
		}

		return outputNumber;
	}

	public int getSide(int orientation) {
		if (side[orientation].average > LIQUID_IN_PIPE)
			return LIQUID_IN_PIPE;
		else
			return side[orientation].average;
	}

	public int getCenter() {
		if (center.average > LIQUID_IN_PIPE)
			return LIQUID_IN_PIPE;
		else
			return center.average;
	}

	@Override
	public int getLiquidQuantity() {
		int total = center.qty;

		for (LiquidBuffer b : side)
			total += b.qty;

		return total;
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		return 0;
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		super.onNeighborBlockChange(blockId);

		for (int i = 0; i < 6; ++i)
			if (!Utils.checkPipesConnections(
					container.getTile(Orientations.values()[i]), container))
				side[i].reset();
	}

	@Override
	public int getLiquidId() {
		return center.liquidId;
	}

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		if (tile instanceof ILiquidContainer) {
			ILiquidContainer liq = (ILiquidContainer) tile;

			if (liq.getLiquidSlots() != null && liq.getLiquidSlots().length > 0)
				return true;
		}

		return tile instanceof TileGenericPipe
    	    || (tile instanceof IMachine && ((IMachine) tile).manageLiquids());
	}

	private static long lastSplit = -1;

	private static int [] splitVector;

	public static int [] getSplitVector (World worldObj) {
		if (lastSplit == worldObj.getWorldTime())
			return splitVector;

		lastSplit = worldObj.getWorldTime();

		splitVector = new int [6];

		for (int i = 0; i < 6; ++i)
			splitVector [i] = i;

		for (int i = 0; i < 20; ++i) {
			int a = worldObj.rand.nextInt(6);
			int b = worldObj.rand.nextInt(6);

			int tmp = splitVector [a];
			splitVector [a] = splitVector [b];
			splitVector [b] = tmp;
		}

		return splitVector;
	}

	public boolean isTriggerActive (Trigger trigger) {
		return false;
	}

	@Override
	public LiquidSlot[] getLiquidSlots() {
		return new LiquidSlot [0];
	}

	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportLiquids;
	}

}

