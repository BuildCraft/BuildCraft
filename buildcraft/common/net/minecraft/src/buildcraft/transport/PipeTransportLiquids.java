package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;
import java.util.TreeMap;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityItem;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraftTransport;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.StackUtil;
import net.minecraft.src.buildcraft.core.TileNetworkData;
import net.minecraft.src.buildcraft.core.Utils;

public class PipeTransportLiquids extends PipeTransport implements ILiquidContainer {

	public static int flowRate = 20;

	public @TileNetworkData(staticSize = 6)
	int[] sideToCenter = new int[6];
	public @TileNetworkData(staticSize = 6)
	int[] centerToSide = new int[6];
	public @TileNetworkData
	int centerIn = 0;
	public @TileNetworkData
	int centerOut = 0;
	public @TileNetworkData
	int liquidId = 0;

	public @TileNetworkData(staticSize = 6)
	boolean[] isInput = new boolean[6];

	public @TileNetworkData
	Orientations lastFromOrientation = Orientations.XPos;
	public @TileNetworkData
	Orientations lastToOrientation = Orientations.XPos;

	private SafeTimeTracker timeTracker = new SafeTimeTracker();

	private boolean blockNeighborChange = false;

	public PipeTransportLiquids() {
		for (int j = 0; j < 6; ++j) {
			sideToCenter[j] = 0;
			centerToSide[j] = 0;
			isInput[j] = false;
		}
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
			sideToCenter[i] = nbttagcompound.getInteger("sideToCenter[" + i
					+ "]");
			centerToSide[i] = nbttagcompound.getInteger("centerToSide[" + i
					+ "]");
			isInput[i] = nbttagcompound.getBoolean("isInput[" + i + "]");
		}

		centerIn = nbttagcompound.getInteger("centerIn");
		centerOut = nbttagcompound.getInteger("centerOut");
		lastFromOrientation = Orientations.values()[nbttagcompound
				.getInteger("lastFromOrientation")];
		lastToOrientation = Orientations.values()[nbttagcompound
				.getInteger("lastToOrientation")];
		liquidId = nbttagcompound.getInteger("liquidId");

		if (liquidId == 0) {
			centerIn = 0;
			centerOut = 0;

			for (int i = 0; i < 6; ++i) {
				centerToSide[i] = 0;
				sideToCenter[i] = 0;
			}
		}
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		for (int i = 0; i < 6; ++i) {
			nbttagcompound.setInteger("sideToCenter[" + i + "]",
					sideToCenter[i]);
			nbttagcompound.setInteger("centerToSide[" + i + "]",
					centerToSide[i]);
			nbttagcompound.setBoolean("isInput[" + i + "]", isInput[i]);
		}

		nbttagcompound.setInteger("centerIn", centerIn);
		nbttagcompound.setInteger("centerOut", centerOut);
		nbttagcompound.setInteger("lastFromOrientation",
				lastFromOrientation.ordinal());
		nbttagcompound.setInteger("lastToOrientation",
				lastToOrientation.ordinal());
		nbttagcompound.setInteger("liquidId", liquidId);
	}
	
	protected void doWork() {
	}

	public void onDropped(EntityItem item) {

	}

	/**
	 * Fills the pipe, and return the amount of liquid that has been used.
	 */
	public int fill(Orientations from, int quantity, int id) {
		if ((getLiquidQuantity() != 0 && liquidId != id) || id == 0) {
			return 0;
		}

		liquidId = id;

		int space = BuildCraftCore.BUCKET_VOLUME / 4
				- sideToCenter[from.ordinal()] - centerToSide[from.ordinal()]
				+ flowRate;

		isInput[from.ordinal()] = true;

		if (space <= 0) {
			return 0;
		}
		
		if (space > flowRate) {
			space = flowRate;
		}
		
		if (space > quantity) {
			sideToCenter[from.ordinal()] += quantity;
			return quantity;
		} else {
			sideToCenter[from.ordinal()] += space;
			return space;
		}
	}

	private void moveLiquids() {
		float centerSpace = BuildCraftCore.BUCKET_VOLUME / 2 - centerIn
				- centerOut + flowRate;

		boolean moved = false;

		// computes the various inputs of liquids

		for (int i = 0; i < 6; ++i) {
			if (isInput[i]) {
				if (centerToSide[i] > 0 && centerSpace >= flowRate) {
					lastFromOrientation = Orientations.values()[i];
					centerToSide[i] -= flowRate;
					centerIn += flowRate;
					moved = true;
				}

				if (sideToCenter[i] + centerToSide[i] >= BuildCraftCore.BUCKET_VOLUME / 4) {
					centerToSide[i] = sideToCenter[i] + centerToSide[i];
					sideToCenter[i] = 0;
				}
			}
		}

		// computes the move from the center

		if (centerIn + centerOut >= BuildCraftCore.BUCKET_VOLUME / 2) {
			centerOut = centerIn + centerOut;
			centerIn = 0;
		}

		// computes the output of liquid
		for (int i = 0; i < 6; ++i) {
			Position p = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[i]);
			p.moveForwards(1);

			if (canReceiveLiquid(p)) {
				if (sideToCenter[i] > 0) {
					ILiquidContainer pipe = (ILiquidContainer) Utils.getTile(
							worldObj, p, Orientations.Unknown);

					sideToCenter[i] -= pipe.fill(p.orientation.reverse(),
							flowRate, liquidId);

					moved = true;
				}

				if (centerOut > 0
						&& sideToCenter[i] + centerToSide[i] <= BuildCraftCore.BUCKET_VOLUME / 4) {
					lastToOrientation = p.orientation;
					centerToSide[i] += flowRate;
					centerOut -= flowRate;

					moved = true;
				}

				if (centerToSide[i] + sideToCenter[i] >= BuildCraftCore.BUCKET_VOLUME / 4) {
					sideToCenter[i] = centerToSide[i] + sideToCenter[i];
					centerToSide[i] = 0;
				}
			}
		}

		if (!moved) {
			for (int i = 0; i < 6; ++i) {
				Position p = new Position(xCoord, yCoord, zCoord,
						Orientations.values()[i]);
				p.moveForwards(1);

				if (canReceiveLiquid(p)) {
					return;
				}
			}

			// If we can't find a direction where to potentially send liquid,
			// reset all input markers
			for (int i = 0; i < 6; ++i) {
				isInput[i] = false;
			}
		}

//		if (totalOil() > 500) {
//			int totalSystem = totalOil();
//
//			for (int i = 0; i < 6; ++i) {
//				Position pos = new Position(xCoord, yCoord, zCoord,
//						Orientations.values()[i]);
//
//				TileEntity tile = Utils.getTile(worldObj, pos,
//						Orientations.values()[i]);
//
//				if (tile instanceof TilePipe) {
//					totalSystem += ((TilePipe) tile).totalOil();
//				}
//			}
//
//			// System.out.println (totalSystem);
//		}
	}

	public int totalOil() {
		int total = centerOut + centerIn;

		for (int i : centerToSide) {
			total += i;
		}

		for (int i : sideToCenter) {
			total += i;
		}

		return total;
	}

	public int getSideToCenter(int orientation) {
		if (sideToCenter[orientation] > BuildCraftCore.BUCKET_VOLUME / 4) {
			return BuildCraftCore.BUCKET_VOLUME / 4;
		} else {
			return sideToCenter[orientation];
		}
	}

	public int getCenterToSide(int orientation) {
		if (centerToSide[orientation] > BuildCraftCore.BUCKET_VOLUME / 4) {
			return BuildCraftCore.BUCKET_VOLUME / 4;
		} else {
			return centerToSide[orientation];
		}
	}

	public int getCenterIn() {
		if (centerIn > BuildCraftCore.BUCKET_VOLUME / 2) {
			return BuildCraftCore.BUCKET_VOLUME / 2;
		} else {
			return centerIn;
		}
	}

	public int getCenterOut() {
		if (centerOut > BuildCraftCore.BUCKET_VOLUME / 2) {
			return BuildCraftCore.BUCKET_VOLUME / 2;
		} else {
			return centerOut;
		}
	}

	@Override
	public int getLiquidQuantity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int empty(int quantityMax, boolean doEmpty) {
		return 0;
	}

	public void scheduleNeighborChange() {
		blockNeighborChange = true;
	}

	protected void neighborChange() {
		for (int i = 0; i < 6; ++i) {
			Position pos = new Position(xCoord, yCoord, zCoord,
					Orientations.values()[i]);

			pos.moveForwards(1);

			if (!canReceiveLiquid(pos)) {
				centerToSide[i] = 0;
				sideToCenter[i] = 0;
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
