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
	
	int travelDelay = 5;
	
	class LiquidBuffer {
		int [] in = new int [travelDelay];
		int ready;
		int [] out = new int [travelDelay];
		int qty;
		int orientation;
		
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
			
			ready = 0;
			qty = 0;			
		}
		
		public int fill (int toFill, boolean doFill) {
			int date = (int) (worldObj.getWorldTime() % travelDelay);
			int newDate = date > 0 ? date - 1 : travelDelay - 1;
			
			if (qty + toFill > LIQUID_IN_PIPE) {
				toFill = LIQUID_IN_PIPE - qty;
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
			int date = (int) (worldObj.getWorldTime() % travelDelay);

			ready += in [date];
			in [date] = 0;
			
			if (out [date] != 0) {
				int extracted = 0;
				
				if (orientation < 6) {
					if (isInput [orientation]) {
						extracted = center.fill(out [date], true);
					} if (isOutput[orientation]) {
						Position p = new Position(xCoord, yCoord, zCoord,
								Orientations.values()[orientation]);
						p.moveForwards(1);

						ILiquidContainer nextPipe = (ILiquidContainer) Utils
								.getTile(worldObj, p, Orientations.Unknown);
						extracted = nextPipe.fill(p.orientation.reverse(),
								out[date], liquidId, true);
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
		}
		
		private int splitLiquid (int quantity, int outputNumber) {
			int extracted = 0;
			
			int slotExtract = (int) Math
			.floor(((double) quantity / (double) outputNumber));
	
			for (int i = 0; i < 6; ++i) {
				int toExtract = slotExtract <= quantity ? slotExtract : quantity;
		
				if (isOutput [i]) {
					extracted += side [i].fill(toExtract, true);
					
					if (extracted != quantity) {
						filled [i] = true;
					}
				}
			}
			
			return extracted;
		}
	}
	
	public int flowRate = 20;

	public @TileNetworkData(staticSize = 6)
	LiquidBuffer[] side = new LiquidBuffer [6];
	public @TileNetworkData
	LiquidBuffer center;
	public @TileNetworkData
	int liquidId = 0;

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

//		for (int i = 0; i < 6; ++i) {
//			side[i] = nbttagcompound.getInteger("side[" + i + "]");
//			isInput[i] = nbttagcompound.getBoolean("isInput[" + i + "]");
//		}
//
//		center = nbttagcompound.getInteger("center");
		liquidId = nbttagcompound.getInteger("liquidId");

//		if (liquidId == 0) {
//			center = 0;
//
//			for (int i = 0; i < 6; ++i) {
//				side[i] = 0;
//			}
//		}
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

//		for (int i = 0; i < 6; ++i) {
//			nbttagcompound.setInteger("side[" + i + "]", side[i]);
//			nbttagcompound.setBoolean("isInput[" + i + "]", isInput[i]);
//		}
//
//		nbttagcompound.setInteger("center", center);
		nbttagcompound.setInteger("liquidId", liquidId);
	}
	
	protected void doWork() {
	}

	public void onDropped(EntityItem item) {

	}

	/**
	 * Fills the pipe, and return the amount of liquid that has been used.
	 */
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if ((getLiquidQuantity() != 0 && liquidId != id) || id == 0) {
			return 0;
		}
				
		liquidId = id;
		isInput[from.ordinal()] = true;
			
		return side[from.ordinal()].fill(quantity, doFill);
	}
	
	private void moveLiquids() {						
		boolean moved = false;
		boolean sendFailed [] = new boolean [] {false, false, false, false, false, false};
		isOutput = new boolean [] {false, false, false, false, false, false};
		
		int outputNumber = 0;
		
		// COMPUTES OUTPUTS

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
				
		
		for (int i = 0; i < 6; ++i) {
			side [i].empty(flowRate);
		}
		
		center.empty(flowRate);
		
		// APPLY SCHEDULED FILLED ORDERS
		
		center.update();
		
		for (int i = 0; i < 6; ++i) {
			side [i].update();
		}
		
//		if (!moved) {		
//			for (int i = 0; i < 6; ++i) {
//				Position p = new Position(xCoord, yCoord, zCoord,
//						Orientations.values()[i]);
//				p.moveForwards(1);
//
//				if (canReceiveLiquid(p) && !sendFailed [i]) {
//					//  If we can send liquids there at some point, exit. 
//					//  Otherwise, we tried to send liquid and that didn't 
//					//  work, so try an other route.
//					
//					return;					
//				}
//			}
//
//			// If we can't find a direction where to potentially send liquid,
//			// reset all input directions
//
//			for (int i = 0; i < 6; ++i) {
//				isInput[i] = false;				
//			}
//		}				
	}

	public int getSide(int orientation) {
		if (side[orientation].qty > LIQUID_IN_PIPE) {
			return LIQUID_IN_PIPE;
		} else {
			return side[orientation].qty;
		}
	}

	public int getCenter() {
		if (center.qty > LIQUID_IN_PIPE) {
			return LIQUID_IN_PIPE;
		} else {
			return center.qty;
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
		return liquidId;
	}
	
	public boolean isPipeConnected(TileEntity tile) {
		return tile instanceof TileGenericPipe 
    	    || tile instanceof ILiquidContainer
    	    || (tile instanceof IMachine && ((IMachine) tile).manageLiquids());
	}
}
