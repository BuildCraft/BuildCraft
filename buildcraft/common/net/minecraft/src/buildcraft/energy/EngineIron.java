package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.Orientations;

public class EngineIron extends Engine {
	
	public static int MAX_LIQUID = BuildCraftCore.BUCKET_VOLUME * 10;
	public static int MAX_HEAT          = 100000;
	public static int COOLANT_THRESHOLD = 55000;
	
	int burnTime = 0;
	int liquidQty = 0;
	int liquidId = 0;	
	
	int coolantQty = 0;
	int coolantId = 0;
	
	int heat = 0;

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
			return 0.05F;
		case Yellow:
			return 0.06F;
		case Red:
			return 0.07F;
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
				heat += currentFuel.powerPerCycle;
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
	public void update () {
		super.update();
		
		if (heat > COOLANT_THRESHOLD) {			
			int extraHeat = heat - COOLANT_THRESHOLD;
			
			if (coolantQty > extraHeat) {
				coolantQty -= extraHeat;
				heat = COOLANT_THRESHOLD;
			} else {
				heat -= coolantQty;
				coolantQty = 0;
			}
		}
		
		if (heat > 0 && !tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord,
						tile.yCoord, tile.zCoord)) {
			if (heat >= 10) {
				heat -= 10;
			} else {
				heat -= 1;
			}
		}
	}
	
	public EnergyStage getEnergyStage () {
		if (heat <= MAX_HEAT / 4) {
			return EnergyStage.Blue;
		} else if (heat <= MAX_HEAT / 2) {
		 	return EnergyStage.Green;
		}  else if (heat <= (float) MAX_HEAT * 3F / 4F) {
			return EnergyStage.Yellow;
		} else if (heat <= MAX_HEAT) {
			return EnergyStage.Red;
		} else {
			return EnergyStage.Explosion;
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) liquidQty / (float) (MAX_LIQUID))
				* (float) i);
	}
	
	public int fill(Orientations from, int quantity, int id) {		
		if (id == Block.waterStill.blockID) {
			return fillCoolant (from, quantity, id);
		}
		
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
	
	private int fillCoolant(Orientations from, int quantity, int id) {
		int res = 0;
		
		if (coolantQty > 0 && coolantId != id) {
			return 0;
		}
		
		if (coolantQty + quantity <= MAX_LIQUID) {
			coolantQty += quantity;
			res = quantity;
		} else {
			res = MAX_LIQUID - coolantQty;
			coolantQty = MAX_LIQUID;
		}
		
		coolantId = id;			
		
		return res;
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		liquidId = nbttagcompound.getInteger("liquidId");
		liquidQty = nbttagcompound.getInteger("liquidQty");
		burnTime = nbttagcompound.getInteger("burnTime");
		coolantId = nbttagcompound.getInteger("coolantId");
		coolantQty = nbttagcompound.getInteger("coolantQty");
		heat = nbttagcompound.getInteger("heat");
    }
    
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	nbttagcompound.setInteger("liquidId", liquidId);
    	nbttagcompound.setInteger("liquidQty", liquidQty);
    	nbttagcompound.setInteger("burnTime", burnTime);
		nbttagcompound.setInteger("coolantId", coolantId);
		nbttagcompound.setInteger("coolantQty", coolantQty);
		nbttagcompound.setInteger("heat", heat);
    }
    
    public int getScaledCoolant(int i) {
        return (int) (((float) coolantQty / (float) (MAX_LIQUID))
				* (float) i);
    }
	
}
