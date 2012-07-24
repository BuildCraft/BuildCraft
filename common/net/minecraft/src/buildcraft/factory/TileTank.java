/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.liquids.ILiquidTank;
import net.minecraft.src.buildcraft.api.liquids.ITankContainer;
import net.minecraft.src.buildcraft.api.liquids.LiquidStack;
import net.minecraft.src.buildcraft.api.liquids.LiquidTank;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.network.TileNetworkData;

public class TileTank extends TileBuildCraft implements ITankContainer {

	public @TileNetworkData
	int stored = 0;
	public @TileNetworkData
	int liquidId = 0;

	public boolean hasUpdate = false;
	public SafeTimeTracker tracker = new SafeTimeTracker();

	/* UPDATING */
	@Override
	public void updateEntity() {
		if (APIProxy.isServerSide() && hasUpdate && tracker.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
			sendNetworkUpdate();
			hasUpdate = false;
		}
		
		if(APIProxy.isRemote())
			return;
		
		// Have liquid flow down into tanks below if any.
		if(stored > 0)
			moveLiquidBelow();
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		stored = nbttagcompound.getInteger("stored");
		liquidId = nbttagcompound.getInteger("liquidId");

		if (liquidId == 0) {
			stored = 0;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("stored", stored);
		nbttagcompound.setInteger("liquidId", liquidId);
	}

	/* HELPER FUNCTIONS */
	/**
	 * @return Last tank block below this one or this one if it is the last.
	 */
	public TileTank getBottomTank() {
		
		TileTank lastTank = this;

		while(true) {
			TileTank below = getTankBelow(lastTank);
			if(below != null) {
				lastTank = below;
			} else
				break;
		}
		
		return lastTank;
	}
	
	public TileTank getTankBelow(TileTank tile) {
		TileEntity below = worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord - 1, tile.zCoord);
		if(below instanceof TileTank)
			return(TileTank)below;
		else
			return null;
			
	}
	
	public TileTank getTankAbove(TileTank tile) {
		TileEntity above = worldObj.getBlockTileEntity(tile.xCoord, tile.yCoord + 1, tile.zCoord);
		if(above instanceof TileTank)
			return(TileTank)above;
		else
			return null;
			
	}
	
	public void moveLiquidBelow() {
		TileTank below = getTankBelow(this);
		if(below == null)
			return;
		if(below.stored >= below.getTankCapacity())
			return;
		if(below.liquidId > 0
				&& below.liquidId != this.liquidId)
			return;
		
		int toMove = Math.min(stored, 100);
		int moved = Math.min(toMove, below.getTankCapacity() - below.stored);
		stored -= moved;
		below.liquidId = liquidId;
		below.stored += moved;
		
	}

	/* ITANKCONTAINER */
	@Override
	public int fill(Orientations from, LiquidStack resource, boolean doFill) {
		return getBottomTank().actualFill(from, resource.amount, resource.itemID, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		return getBottomTank().actualFill(Orientations.YPos, resource.amount, resource.itemID, doFill);
	}

	@Override
	public LiquidStack drain(Orientations from, int maxEmpty, boolean doDrain) {
		int drained = getBottomTank().actualEmtpy(maxEmpty, doDrain);
		return new LiquidStack(liquidId, drained);
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxEmpty, boolean doDrain) {
		int drained = getBottomTank().actualEmtpy(maxEmpty, doDrain);
		return new LiquidStack(liquidId, drained);
	}

	@Override
	public ILiquidTank[] getTanks() {
		int resultLiquidId = 0;
		int resultLiquidQty = 0;
		int resultCapacity = 0;

		if (stored != 0) {
			resultLiquidId = liquidId;
		}

		resultLiquidQty += stored;
		resultCapacity += getTankCapacity();

		for (int ySearch = yCoord - 1; ySearch >= 0; --ySearch) {
			if (worldObj.getBlockId(xCoord, ySearch, zCoord) != BuildCraftFactory.tankBlock.blockID) {
				break;
			}

			TileTank tank = (TileTank) worldObj.getBlockTileEntity(xCoord, ySearch, zCoord);

			if (tank.stored != 0) {
				resultLiquidId = tank.liquidId;
			}

			resultLiquidQty += tank.stored;
			resultCapacity += tank.getTankCapacity();
		}

		for (int ySearch = yCoord + 1; ySearch < 128; ++ySearch) {
			if (worldObj.getBlockId(xCoord, ySearch, zCoord) != BuildCraftFactory.tankBlock.blockID) {
				break;
			}

			TileTank tank = (TileTank) worldObj.getBlockTileEntity(xCoord, ySearch, zCoord);

			if (tank.stored != 0) {
				resultLiquidId = tank.liquidId;
			}

			resultLiquidQty += tank.stored;
			resultCapacity += tank.getTankCapacity();
		}

		return new ILiquidTank[] { new LiquidTank(resultLiquidId, resultLiquidQty, resultCapacity) };
	}
	
	private int actualFill(Orientations from, int quantity, int id, boolean doFill) {
		if (stored != 0 && id != liquidId)
			return 0;

		liquidId = id;
		int used = 0;

		TileTank above = getTankAbove(this);

		if (stored + quantity <= getTankCapacity()) {
			if (doFill) {
				stored += quantity;
				hasUpdate = true;
			}

			used = quantity;
		} else if (stored <= getTankCapacity()) {
			used = getTankCapacity() - stored;

			if (doFill) {
				stored = getTankCapacity();
				hasUpdate = true;
			}
		}

		if (used < quantity && above != null)
			used = used + above.actualFill(from, quantity - used, id, doFill);

		return used;
	}

	public int getTankCapacity() {
		return BuildCraftAPI.BUCKET_VOLUME * 16;
	}
	
	public LiquidStack getLiquid() {
		return new LiquidStack(liquidId, stored, 0);
	}

	private int actualEmtpy(int quantityMax, boolean doEmpty) {
		
		if (stored >= quantityMax) {
			if (doEmpty) {
				stored -= quantityMax;
				hasUpdate = true;
			}

			return quantityMax;
			
		} else {
			int result = stored;

			if (doEmpty) {
				stored = 0;
				hasUpdate = true;
			}

			TileTank below = getTankBelow(this);

			if (below != null)
				result += below.actualEmtpy(quantityMax - result, doEmpty);

			return result;
		}
	}

}
