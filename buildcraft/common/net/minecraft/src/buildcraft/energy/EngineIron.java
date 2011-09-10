package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.Orientations;

public class EngineIron extends Engine {
	
	public static int MAX_LIQUID = BuildCraftCore.BUCKET_VOLUME * 10;
	
	int burnTime = 0;
	int liquidQty = 0;
	int liquidId = 0;	

	public EngineIron(TileEngine engine) {
		super(engine);
		
		maxEnergy = 100000;
		maxEnergyExtracted = 500;
	}
	
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_iron.png";
	}
	
	public int explosionRange () {
		return 8;
	}
	
	public int maxEnergyReceived () {
		return 2000;
	}

	public float getPistonSpeed () {
		switch (getEnergyStage()) {
		case Blue:
			return 0.04F;
		case Green:
			return 0.08F;
		case Yellow:
			return 0.16F;
		case Red:
			return 0.32F;
		}
		
		return 0;
	}
	
	public boolean isBurning () {
		return liquidQty > 0
				&& tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord,
						tile.yCoord, tile.zCoord);
	}
	
	public void burn () {
		EngineFuel currentFuel = TileEngine.possibleFuels.get(liquidId);
		
		if (currentFuel == null) {
			return;
		}
		
		if (tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord,
				tile.yCoord, tile.zCoord)) {
			
			if(burnTime > 0 || liquidQty > 0) {
				if (burnTime > 0) {
					burnTime--;
				} else {
					liquidQty--;
					burnTime = currentFuel.totalBurningTime / BuildCraftCore.BUCKET_VOLUME;
				}
				
				addEnergy(currentFuel.powerPerCycle);					
			}
		}

		ItemStack itemInInventory = tile.getStackInSlot(0);
				
		if (itemInInventory != null
				&& itemInInventory.itemID == BuildCraftEnergy.bucketOil.shiftedIndex) {

			if (liquidQty + BuildCraftCore.BUCKET_VOLUME <= MAX_LIQUID) {
				itemInInventory = new ItemStack(Item.bucketEmpty, 1);
				liquidQty += BuildCraftCore.BUCKET_VOLUME;
			}
		}				
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) liquidQty / (float) (MAX_LIQUID))
				* (float) i);
	}
	
	public int fill(Orientations from, int quantity, int id) {
		int res = 0;
		
		if (liquidQty > 0 && liquidId != id) {
			return 0;
		}
		
		if (!TileEngine.possibleFuels.containsKey(id)) {
			return 0;
		}
		
		if (liquidQty + quantity <= MAX_LIQUID) {
			liquidQty += quantity;
			res = quantity;
		} else {
			res = MAX_LIQUID - liquidQty;
			liquidQty = MAX_LIQUID;
		}
		
		liquidId = id;				
		
		return res;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		liquidId = nbttagcompound.getInteger("liquidId");
		liquidQty = nbttagcompound.getInteger("liquidQty");
		burnTime = nbttagcompound.getInteger("burnTime");
    }
    
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	nbttagcompound.setInteger("liquidId", liquidId);
    	nbttagcompound.setInteger("liquidQty", liquidQty);
    	nbttagcompound.setInteger("burnTime", burnTime);
    }
}
