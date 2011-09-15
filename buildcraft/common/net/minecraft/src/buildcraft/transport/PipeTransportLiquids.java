package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityItem;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.TileNetworkData;
import net.minecraft.src.buildcraft.core.Utils;

public class PipeTransportLiquids extends PipeTransport implements ILiquidContainer {
	

	/**
	 * The amount of liquid contained by a pipe section. For simplicity, all
	 * pipe sections are assumed to be of the same volume.
	 */
	public static int LIQUID_IN_PIPE = BuildCraftCore.BUCKET_VOLUME / 4;
	
	public int travelDelay = 6;	
	public int flowRate = 20;
	
	public class LiquidBuffer {
		short [] in = new short [travelDelay];
		short ready;
		short [] out = new short [travelDelay];
		short qty;
		short liquidId = 0;
		int orientation;
		
		short [] lastQty = new short [100];
		int lastTotal = 0; 
		int auverage;
		
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
			
			for (int i = 0; i < lastQty.length; ++i) {
				lastQty [i] = 0;
			}
			
			ready = 0;
			qty = 0;			
			liquidId = 0;
			lastTotal = 0;
		}
		
		public int fill (int toFill, boolean doFill, short liquidId) {
			if (qty > 0 && this.liquidId != liquidId && this.liquidId != 0) {
				return 0;
			}
			
			if (this.liquidId != liquidId) {
				reset ();
			}
			
			this.liquidId = liquidId;
			
			int date = (int) (worldObj.getWorldTime() % travelDelay);
			int newDate = date > 0 ? date - 1 : travelDelay - 1;
			
			if (qty + toFill > LIQUID_IN_PIPE + flowRate) {
				toFill = LIQUID_IN_PIPE + flowRate - qty;
			}
			
			if (doFill) {
				qty += toFill;			
				in [newDate] += toFill;
			}
			
			return toFill;
		}
		
		public int empty (int toEmpty) {
			int date = (int) (worldObj.getWorldTime() % travelDelay);
			int newDate = date > 0 ? date - 1 : travelDelay - 1;
			
			if (ready - toEmpty < 0) {
				toEmpty = ready;
			}
			
			ready -= toEmpty;
			
			out [newDate] += toEmpty;
			
			return toEmpty;
		}
		
		public void update () {
			bouncing = false;
			
			if (qty == 0) {
				return;
			}
			
			int date = (int) (worldObj.getWorldTime() % travelDelay);

			ready += in [date];
			in [date] = 0;
			
			if (out [date] != 0) {
				int extracted = 0;
				
				if (orientation < 6) {
					if (isInput [orientation]) {
						extracted = center.fill(out [date], true, liquidId);
					} if (isOutput[orientation]) {
						Position p = new Position(xCoord, yCoord, zCoord,
								Orientations.values()[orientation]);
						p.moveForwards(1);

						ILiquidContainer nextPipe = (ILiquidContainer) Utils
								.getTile(worldObj, p, Orientations.Unknown);
						extracted = nextPipe.fill(p.orientation.reverse(),
								out[date], liquidId, true);
						
						if (extracted == 0) {
							bouncing = true;
							extracted += center.fill(out [date], true, liquidId);
						}
					}
				} else {
					int outputNumber = 0;
					
					for (int i = 0; i < 6; ++i) {
						if (isOutput [i]) {
							outputNumber++;
						}
					}
					
					filled = new boolean [] {false, false, false, false, false, false};
					
					// try first, to detect filled outputs
					extracted = splitLiquid(out [date], outputNumber);
					
					if (extracted < out [date]) {
						outputNumber = 0;
						
						// try a second time, if to split the remaining in non
						// filled if any
						for (int i = 0; i < 6; ++i) {
							if (isOutput [i] && !filled [i]) {
								outputNumber++;
							}
						}
						
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
			
			auverage = lastTotal / lastQty.length;			
		}
		
		private int splitLiquid (int quantity, int outputNumber) {
			int extracted = 0;
			
			int slotExtract = (int) Math
			.ceil(((double) quantity / (double) outputNumber));
	
			for (int i = worldObj.rand.nextInt(6); i < 6; ++i) {
				int toExtract = slotExtract <= quantity ? slotExtract : quantity;				
		
				if (isOutput [i] && !filled [i]) {
					extracted += side [i].fill(toExtract, true, liquidId);
					quantity -= toExtract;
					
					if (extracted != toExtract) {
						filled [i] = true;
					}
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

	public @TileNetworkData(staticSize = 6)
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
	
	public boolean canReceiveLiquid(Position p) {
		TileEntity entity = worldObj.getBlockTileEntity((int) p.x, (int) p.y,
				(int) p.z);

		if (isInput[p.orientation.ordinal()]) {
			return false;
		}

		if (!Utils.checkPipesConnections(worldObj, (int) p.x, (int) p.y,
				(int) p.z, xCoord, yCoord, zCoord)) {
			return false;
		}

		if (entity instanceof IPipeEntry || entity instanceof ILiquidContainer) {
			return true;
		}

		return false;
	}

	public void updateEntity() {
		moveLiquids();

//		if (APIProxy.isServerSide()) {
//			if (timeTracker.markTimeIfDelay(worldObj, 50)) {
//				sendNetworkUpdate();
//			}
//		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			if (nbttagcompound.hasKey("side[" + i + "]")) {
				side [i].readFromNBT(nbttagcompound.getCompoundTag("side[" + i + "]"));
			}
			
			isInput [i] = nbttagcompound.getBoolean("isInput[" + i + "]");
		}
		
		if (nbttagcompound.hasKey("center")) {
			center.readFromNBT(nbttagcompound.getCompoundTag("center"));
		}
		
		NBTTagCompound sub = new NBTTagCompound();
		center.writeToNBT(sub);
		nbttagcompound.setTag("center", sub);		
	}

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
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		isInput[from.ordinal()] = true;
			
		return side[from.ordinal()].fill(quantity, doFill, (short) id);
	}
	
	private void moveLiquids() {
		isOutput = new boolean [] {false, false, false, false, false, false};
		
		int outputNumber = computeOutputs ();
		
		if (outputNumber == 0) {	
			for (int i = 0; i < 6; ++i) {
				isInput[i] = false;				
			}
			
			outputNumber = computeOutputs();
		}		
		
		for (int i = 0; i < 6; ++i) {
			side [i].empty(flowRate);
		}
		
		center.empty(flowRate);
		
		// APPLY SCHEDULED FILLED ORDERS
		
		center.update();
		
		for (int i = 0; i < 6; ++i) {
			side [i].update();
			
			if (side [i].bouncing) {
				isInput [i] = true;
			} else if (side [i].qty == 0) {
				isInput [i] = false;
			}
		}	
	}

	private int computeOutputs() {
		int outputNumber = 0;
		
		for (int i = 0; i < 6; ++i) {
			Position p = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[i]);
			p.moveForwards(1);
			
			isOutput [i] = container.pipe.outputOpen(p.orientation)
					&& canReceiveLiquid(p) && !isInput[i];
			
			if (isOutput [i]) {
				outputNumber++;
			}
		}		
				
		return outputNumber;
	}

	public int getSide(int orientation) {
		if (side[orientation].auverage > LIQUID_IN_PIPE) {
			return LIQUID_IN_PIPE;
		} else {
			return side[orientation].auverage;
		}
	}

	public int getCenter() {
		if (center.auverage > LIQUID_IN_PIPE) {
			return LIQUID_IN_PIPE;
		} else {
			return center.auverage;
		}
	}

	@Override
	public int getLiquidQuantity() {
		int total = center.qty;

		for (LiquidBuffer b : side) {
			total += b.qty;
		}

		return total;
	}

	@Override
	public int getCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int empty(int quantityMax, boolean doEmpty) {
		return 0;
	}

	@Override
	public void onNeighborBlockChange() {
		super.onNeighborBlockChange();
		
		for (int i = 0; i < 6; ++i) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[i]);

			pos.moveForwards(1);

			if (!canReceiveLiquid(pos)) {				
				side[i].reset ();
			}
		}
	}

	public int getLiquidId() {
		return center.liquidId;
	}
	
	public boolean isPipeConnected(TileEntity tile) {
		return tile instanceof TileGenericPipe 
    	    || tile instanceof ILiquidContainer
    	    || (tile instanceof IMachine && ((IMachine) tile).manageLiquids());
	}
}

