package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.ILiquidContainer;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.TileNetworkData;

public class TileTank extends TileBuildCraft implements ILiquidContainer {
	
	public @TileNetworkData int stored = 0;
	
	@Override
	public int fill(Orientations from, int quantity) {
		TileTank lastTank = this;
		
		for (int j = yCoord - 1; j > 1; --j) {
			if (worldObj.getBlockTileEntity(xCoord, j, zCoord) instanceof TileTank) {
				lastTank = (TileTank) worldObj.getBlockTileEntity(xCoord, j, zCoord);
			} else {
				break;
			}
		}
		
		return lastTank.actualFill(from, quantity);				
	}
	
	private int actualFill(Orientations from, int quantity) {
		TileEntity above = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
	
		int used = 0;
		
		if (stored + quantity <= getCapacity()) {
			stored += quantity;
			used = quantity;
		} else if (stored <= getCapacity()) {
			used = getCapacity() - stored;			
			stored = getCapacity();		
		}
		
		if (APIProxy.isServerSide() && used > 0) {
			sendNetworkUpdate();
		}
				
		if (used < quantity && above instanceof TileTank) {
			used = used + ((TileTank) above).actualFill(from, quantity - used);
		}		
		
		return used;
	}
	
	public int getLiquidQuantity () {
		return stored;
	}
	
	public int getCapacity () {
		return BuildCraftCore.OIL_BUCKET_QUANTITY * 16;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		stored = nbttagcompound.getInteger("stored");		
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		nbttagcompound.setInteger("stored", stored);
	}
	
	@Override
	public int empty (int quantityMax, boolean doEmpty) {
		TileTank lastTank = this;
		
		for (int j = yCoord + 1; j <= 128; ++j) {
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
			}
			
			return quantityMax;
		} else {
			int result = stored;
			
			if (doEmpty) {
				stored = 0;
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
	
	
	
}
