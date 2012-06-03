/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Block;
import net.minecraft.src.ICrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.IronEngineFuel;
import net.minecraft.src.buildcraft.api.LiquidSlot;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.Utils;

public class EngineIron extends Engine {
	
	public static int MAX_LIQUID = BuildCraftAPI.BUCKET_VOLUME * 10;
	public static int MAX_HEAT          = 100000;
	public static int COOLANT_THRESHOLD = 49000;
	
	int burnTime = 0;
	int liquidQty = 0;
	int liquidId = 0;	
	
	int coolantQty = 0;
	int coolantId = 0;
	
	int heat = 0;
	
	public int penatlyCooling = 0;
	
	boolean lastPowered = false;

	public EngineIron(TileEngine engine) {
		super(engine);
		
		maxEnergy = 100000;
		maxEnergyExtracted = 500;
	}
	
	@Override
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_iron.png";
	}
	
	@Override
	public int explosionRange () {
		return 8;
	}
	
	@Override
	public int maxEnergyReceived () {
		return 2000;
	}

	@Override
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
	
	@Override
	public boolean isBurning () {
		return liquidQty > 0
				&& penatlyCooling == 0
				&& tile.isRedstonePowered;
	}
	
	@Override
	public void burn () {
		currentOutput = 0;
		IronEngineFuel currentFuel = BuildCraftAPI.ironEngineFuel.get(liquidId);
		
		if (currentFuel == null) {
			return;
		}
		
		if (penatlyCooling <= 0 &&
				tile.isRedstonePowered) {
			
			lastPowered = true;
			
			if(burnTime > 0 || liquidQty > 0) {
				if (burnTime > 0) {
					burnTime--;
				} else {
					liquidQty--;
					burnTime = currentFuel.totalBurningTime / BuildCraftAPI.BUCKET_VOLUME;
				}
				
				currentOutput = currentFuel.powerPerCycle;
				addEnergy(currentFuel.powerPerCycle);			
				heat += currentFuel.powerPerCycle;
			}
		} else if (penatlyCooling <= 0) {
			if (lastPowered) {
				lastPowered = false;
				penatlyCooling = 30 * 20; 
				// 30 sec of penalty on top of the cooling				
			}
		}
	}
	
	@Override
	public void update () {
		super.update();
		
		ItemStack itemInInventory = tile.getStackInSlot(0);
		
		if (itemInInventory != null) {
			int liquidId = BuildCraftAPI.getLiquidForFilledItem (itemInInventory);

			if (liquidId != 0) {
				if (fill(Orientations.Unknown, BuildCraftAPI.BUCKET_VOLUME,
						liquidId, false) == BuildCraftAPI.BUCKET_VOLUME) {
					fill(Orientations.Unknown, BuildCraftAPI.BUCKET_VOLUME,
							liquidId, true);

					tile.setInventorySlotContents(0,
							Utils.consumeItem(itemInInventory));
				}
			}
		}	
		
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
		
		if (heat > 0
				&& (penatlyCooling > 0 || !tile.isRedstonePowered)) {
			heat -= 10;
			
		}
		
		if (heat <= 0 && penatlyCooling > 0) {
			penatlyCooling--;
		}
	}
	
	@Override
	public void computeEnergyStage () {
		if (heat <= MAX_HEAT / 4) {
			energyStage = EnergyStage.Blue;
		} else if (heat <= MAX_HEAT / 2) {
			energyStage = EnergyStage.Green;
		}  else if (heat <= (float) MAX_HEAT * 3F / 4F) {
			energyStage = EnergyStage.Yellow;
		} else if (heat <= MAX_HEAT) {
			energyStage = EnergyStage.Red;
		} else {
			energyStage = EnergyStage.Explosion;
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) liquidQty / (float) (MAX_LIQUID))
				* (float) i);
	}
	
	public int fill(Orientations from, int quantity, int id, boolean doFill) {		
		if (id == Block.waterStill.blockID) {
			return fillCoolant (from, quantity, id, doFill);
		}
		
		int res = 0;
		
		if (liquidQty > 0 && liquidId != id) {
			return 0;
		}
		
		if (!BuildCraftAPI.ironEngineFuel.containsKey(id)) {
			return 0;
		}
		
		if (liquidQty + quantity <= MAX_LIQUID) {
			if (doFill) {
				liquidQty += quantity;
			}
			
			res = quantity;
		} else {
			res = MAX_LIQUID - liquidQty;
			
			if (doFill) {
				liquidQty = MAX_LIQUID;
			}
		}
		
		liquidId = id;				
		
		return res;
	}
	
	private int fillCoolant(Orientations from, int quantity, int id, boolean doFill) {
		int res = 0;
		
		if (coolantQty > 0 && coolantId != id) {
			return 0;
		}
		
		if (coolantQty + quantity <= MAX_LIQUID) {
			if (doFill) {
				coolantQty += quantity;
			}
			
			res = quantity;
		} else {
			res = MAX_LIQUID - coolantQty;
			
			if (doFill) {
				coolantQty = MAX_LIQUID;
			}
		}
		
		coolantId = id;			
		
		return res;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		liquidId = nbttagcompound.getInteger("liquidId");
		liquidQty = nbttagcompound.getInteger("liquidQty");
		burnTime = nbttagcompound.getInteger("burnTime");
		coolantId = nbttagcompound.getInteger("coolantId");
		coolantQty = nbttagcompound.getInteger("coolantQty");
		heat = nbttagcompound.getInteger("heat");
		penatlyCooling = nbttagcompound.getInteger("penaltyCooling");
    }
    
	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	nbttagcompound.setInteger("liquidId", liquidId);
    	nbttagcompound.setInteger("liquidQty", liquidQty);
    	nbttagcompound.setInteger("burnTime", burnTime);
		nbttagcompound.setInteger("coolantId", coolantId);
		nbttagcompound.setInteger("coolantQty", coolantQty);
		nbttagcompound.setInteger("heat", heat);
		nbttagcompound.setInteger("penaltyCooling", penatlyCooling);
    }
    
    public int getScaledCoolant(int i) {
        return (int) (((float) coolantQty / (float) (MAX_LIQUID))
				* (float) i);
    }
	
    @Override
	public void delete() {
	
	}
    
    @Override
	public void getGUINetworkData(int i, int j) {
		switch (i) {
		case 0:
			energy = j;
			break;
		case 1:
			currentOutput = j;
			break;
		case 2:
			heat = j;
			break;
		case 3:
			liquidQty = j;
			break;
		case 4:
			liquidId = j;
			break;
		case 5:
			coolantQty = j;
			break;
		case 6:
			coolantId = j;
			break;
		}		
	}

    @Override
	public void sendGUINetworkData(ContainerEngine containerEngine,
			ICrafting iCrafting) {
		iCrafting.updateCraftingInventoryInfo(containerEngine, 0, energy);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 1, currentOutput);	
		iCrafting.updateCraftingInventoryInfo(containerEngine, 2, heat);	
		iCrafting.updateCraftingInventoryInfo(containerEngine, 3, liquidQty);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 4, liquidId);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 5, coolantQty);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 6, coolantId);
	}
	
    @Override
	public LiquidSlot[] getLiquidSlots() {
		return new LiquidSlot[] {
				new LiquidSlot(liquidId, liquidQty, MAX_LIQUID),
				new LiquidSlot(coolantId, coolantQty, MAX_LIQUID) };
	}
	
    @Override
	public boolean isActive() {
		return penatlyCooling <= 0;
	}
    
	@Override public int getHeat() { return heat; }
}
