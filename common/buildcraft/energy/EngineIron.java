/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.Orientations;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.liquids.LiquidManager;
import buildcraft.api.liquids.LiquidStack;
import buildcraft.api.liquids.LiquidTank;
import buildcraft.core.DefaultProps;
import buildcraft.core.Utils;
import buildcraft.energy.gui.ContainerEngine;
import net.minecraft.src.ICrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class EngineIron extends Engine {

	public static int MAX_LIQUID = BuildCraftAPI.BUCKET_VOLUME * 10;
	public static int MAX_HEAT = 100000;
	public static int COOLANT_THRESHOLD = 49000;

	private ItemStack itemInInventory;

	int burnTime = 0;
	int liquidQty = 0;
	public int liquidId = 0;

	int coolantQty = 0;
	public int coolantId = 0;

	int heat = 0;

	public int penaltyCooling = 0;

	boolean lastPowered = false;

	public EngineIron(TileEngine engine) {
		super(engine);

		maxEnergy = 100000;
		maxEnergyExtracted = 500;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_PATH_BLOCKS + "/base_iron.png";
	}

	@Override
	public int explosionRange() {
		return 8;
	}

	@Override
	public int maxEnergyReceived() {
		return 2000;
	}

	@Override
	public float getPistonSpeed() {
		switch (getEnergyStage()) {
		case Blue:
			return 0.04F;
		case Green:
			return 0.05F;
		case Yellow:
			return 0.06F;
		case Red:
			return 0.07F;
		default:
			return 0.0f;
		}
	}

	@Override
	public boolean isBurning() {
		return liquidQty > 0 && penaltyCooling == 0 && tile.isRedstonePowered;
	}

	@Override
	public void burn() {
		currentOutput = 0;
		IronEngineFuel currentFuel = IronEngineFuel.getFuelForLiquid(new LiquidStack(liquidId, liquidQty, 0));

		if (currentFuel == null) {
			return;
		}

		if (penaltyCooling <= 0 && tile.isRedstonePowered) {

			lastPowered = true;

			if (burnTime > 0 || liquidQty > 0) {
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
		} else if (penaltyCooling <= 0) {
			if (lastPowered) {
				lastPowered = false;
				penaltyCooling = 30 * 20;
				// 30 sec of penalty on top of the cooling
			}
		}
	}

	@Override
	public void update() {
		super.update();

		if (itemInInventory != null) {
			LiquidStack liquid = LiquidManager.getLiquidForFilledItem(itemInInventory); 

			if (liquid != null) {
				if (fill(Orientations.Unknown, liquid, false) == liquid.amount) {
					fill(Orientations.Unknown, liquid, true);
					tile.setInventorySlotContents(0, Utils.consumeItem(itemInInventory));
				}
			}
		}

		if (heat > COOLANT_THRESHOLD) {
			int extraHeat = heat - COOLANT_THRESHOLD;

			IronEngineCoolant currentCoolant = IronEngineCoolant.getCoolantForLiquid(new LiquidStack(coolantId, coolantQty, 0));
			if (currentCoolant != null)
			{
				if(coolantQty * currentCoolant.coolingPerUnit > extraHeat) {
					coolantQty -= Math.round(extraHeat / currentCoolant.coolingPerUnit);
					heat = COOLANT_THRESHOLD;
				} else {
					heat -= coolantQty * currentCoolant.coolingPerUnit;
					coolantQty = 0;
				}
			}
		}

		if (heat > 0 && (penaltyCooling > 0 || !tile.isRedstonePowered)) {
			heat -= 10;

		}

		if (heat <= 0 && penaltyCooling > 0) {
			penaltyCooling--;
		}
	}

	@Override
	public void computeEnergyStage() {
		if (heat <= MAX_HEAT / 4) {
			energyStage = EnergyStage.Blue;
		} else if (heat <= MAX_HEAT / 2) {
			energyStage = EnergyStage.Green;
		} else if (heat <= MAX_HEAT * 3F / 4F) {
			energyStage = EnergyStage.Yellow;
		} else if (heat <= MAX_HEAT) {
			energyStage = EnergyStage.Red;
		} else {
			energyStage = EnergyStage.Explosion;
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) liquidQty / (float) (MAX_LIQUID)) * i);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		liquidId = nbttagcompound.getInteger("liquidId");
		liquidQty = nbttagcompound.getInteger("liquidQty");
		burnTime = nbttagcompound.getInteger("burnTime");
		coolantId = nbttagcompound.getInteger("coolantId");
		coolantQty = nbttagcompound.getInteger("coolantQty");
		heat = nbttagcompound.getInteger("heat");
		penaltyCooling = nbttagcompound.getInteger("penaltyCooling");
				
		if (nbttagcompound.hasKey("itemInInventory")) {
			NBTTagCompound cpt = nbttagcompound.getCompoundTag("itemInInventory");
			itemInInventory = ItemStack.loadItemStackFromNBT(cpt);
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("liquidId", liquidId);
		nbttagcompound.setInteger("liquidQty", liquidQty);
		nbttagcompound.setInteger("burnTime", burnTime);
		nbttagcompound.setInteger("coolantId", coolantId);
		nbttagcompound.setInteger("coolantQty", coolantQty);
		nbttagcompound.setInteger("heat", heat);
		nbttagcompound.setInteger("penaltyCooling", penaltyCooling);	
		
		if (itemInInventory != null) {
			NBTTagCompound cpt = new NBTTagCompound();
			itemInInventory.writeToNBT(cpt);
			nbttagcompound.setTag("itemInInventory", cpt);
		}

	}

	public int getScaledCoolant(int i) {
		return (int) (((float) coolantQty / (float) (MAX_LIQUID)) * i);
	}

	@Override
	public void delete() {

	}

	@Override
	public void getGUINetworkData(int i, int j) {
		switch (i) {
		case 0:
			energy = j / 10;
			break;
		case 1:
			currentOutput = j / 10;
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
	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		iCrafting.updateCraftingInventoryInfo(containerEngine, 0, Math.round(energy * 10));
		iCrafting.updateCraftingInventoryInfo(containerEngine, 1, Math.round(currentOutput * 10));
		iCrafting.updateCraftingInventoryInfo(containerEngine, 2, heat);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 3, liquidQty);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 4, liquidId);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 5, coolantQty);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 6, coolantId);
	}

	@Override
	public boolean isActive() {
		return penaltyCooling <= 0;
	}

	@Override
	public int getHeat() {
		return heat;
	}
	
	/* ITANKCONTAINER */
	public int fill(Orientations from, LiquidStack resource, boolean doFill) {
		
		// Handle coolant
		if (IronEngineCoolant.getCoolantForLiquid(resource) != null)
			return fillCoolant(from, resource, doFill);

		int res = 0;

		if (liquidQty > 0 && liquidId != resource.itemID) {
			return 0;
		}

		if (IronEngineFuel.getFuelForLiquid(resource) == null)
			return 0;

		if (liquidQty + resource.amount <= MAX_LIQUID) {
			if (doFill) {
				liquidQty += resource.amount;
			}

			res = resource.amount;
		} else {
			res = MAX_LIQUID - liquidQty;

			if (doFill) {
				liquidQty = MAX_LIQUID;
			}
		}

		liquidId = resource.itemID;

		return res;
	}

	private int fillCoolant(Orientations from, LiquidStack resource, boolean doFill) {
		int res = 0;

		if (coolantQty > 0 && coolantId != resource.itemID)
			return 0;

		if (coolantQty + resource.amount <= MAX_LIQUID) {
			if (doFill)
				coolantQty += resource.amount;

			res = resource.amount;
		} else {
			res = MAX_LIQUID - coolantQty;

			if (doFill)
				coolantQty = MAX_LIQUID;
		}

		coolantId = resource.itemID;

		return res;
	}

	@Override
	public LiquidTank[] getLiquidSlots() {
		return new LiquidTank[] { new LiquidTank(liquidId, liquidQty, MAX_LIQUID),
				new LiquidTank(coolantId, coolantQty, MAX_LIQUID) };
	}

	
	/* IINVENTORY */
	@Override public int getSizeInventory() { return 1; }
	@Override public ItemStack getStackInSlot(int i) { return itemInInventory; }
	@Override public void setInventorySlotContents(int i, ItemStack itemstack) { itemInInventory = itemstack; }

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (itemInInventory != null) {
			ItemStack newStack = itemInInventory.splitStack(j);

			if (itemInInventory.stackSize == 0) {
				itemInInventory = null;
			}

			return newStack;
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (itemInInventory == null)
			return null;
		ItemStack toReturn = itemInInventory;
		itemInInventory = null;
		return toReturn;
	}

}
