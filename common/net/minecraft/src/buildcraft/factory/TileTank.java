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
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.API;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.SafeTimeTracker;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.TileBuildCraft;

public class TileTank extends TileBuildCraft implements ILiquidContainer {
	
	public @TileNetworkData int stored = 0;
	public @TileNetworkData int liquidId = 0;
	
	public boolean hasUpdate = false;
	public SafeTimeTracker tracker = new SafeTimeTracker();
	
	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		TileTank lastTank = this;
		
		for (int j = yCoord - 1; j > 1; --j) {
			if (worldObj.getBlockTileEntity(xCoord, j, zCoord) instanceof TileTank) {
				lastTank = (TileTank) worldObj.getBlockTileEntity(xCoord, j, zCoord);
			} else {
				break;
			}
		}
		
		return lastTank.actualFill(from, quantity, id, doFill);				
	}
	
	private int actualFill(Orientations from, int quantity, int id, boolean doFill) {
		if (stored != 0 && id != liquidId) {
			return 0;
		}
		
		liquidId = id;
		
		TileEntity above = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
	
		int used = 0;
		
		if (stored + quantity <= getCapacity()) {
			if (doFill) {
				stored += quantity;
				hasUpdate = true;
			}
			
			used = quantity;
		} else if (stored <= getCapacity()) {
			used = getCapacity() - stored;		
			
			if (doFill) {
				stored = getCapacity();
				hasUpdate = true;
			}
		}
				
		if (used < quantity && above instanceof TileTank) {
			used = used + ((TileTank) above).actualFill(from, quantity - used, id, doFill);
		}		
		
		return used;
	}
	
	public int getLiquidQuantity () {
		return stored;
	}
	
	public int getCapacity () {
		return API.BUCKET_VOLUME * 16;
	}
	
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
	
	@Override
	public int empty (int quantityMax, boolean doEmpty) {
		TileTank lastTank = this;
		
		for (int j = yCoord + 1; j <= DefaultProps.WORLD_HEIGHT; ++j) {
			if (worldObj.getBlockTileEntity(xCoord, j, zCoord) instanceof TileTank) {
				lastTank = (TileTank) worldObj.getBlockTileEntity(xCoord, j, zCoord);
			} else {
				break;
			}
		}
		
		return lastTank.actualEmtpy(quantityMax, doEmpty);		
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
		
			TileEntity under = worldObj.getBlockTileEntity(xCoord, yCoord - 1,
					zCoord);
			
			if (under instanceof TileTank) {
				result += ((TileTank) under).actualEmtpy(quantityMax - result,
						doEmpty);
			}
			
			return result;
		}
	}

	@Override
	public int getLiquidId() {
		return liquidId;
	}	
	
	@TileNetworkData
	public void updateEntity () {
		if (APIProxy.isServerSide() && hasUpdate
				&& tracker.markTimeIfDelay(worldObj, 2 * BuildCraftCore.updateFactor)) {
			sendNetworkUpdate();
			hasUpdate = false;
		}
	}
	
}
