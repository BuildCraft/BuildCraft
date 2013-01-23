/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import net.minecraft.block.Block;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import buildcraft.api.fuels.IronEngineCoolant;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;
import buildcraft.energy.gui.ContainerEngine;

public class EngineIron extends Engine {

	public static int MAX_LIQUID = LiquidContainerRegistry.BUCKET_VOLUME * 10;
	public static int MAX_HEAT = 100000;
	public static int COOLANT_THRESHOLD = 49000;

	private ItemStack itemInInventory;

	int burnTime = 0;
	int heat = 0;
	private LiquidTank fuelTank;
	private LiquidTank coolantTank;
	private IronEngineFuel currentFuel = null;

	public int penaltyCooling = 0;

	boolean lastPowered = false;

	public EngineIron(TileEngine engine) {
		super(engine);

		maxEnergy = 100000;
		maxEnergyExtracted = 500;
		fuelTank = new LiquidTank(MAX_LIQUID);
		coolantTank = new LiquidTank(MAX_LIQUID);
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
		LiquidStack fuel = fuelTank.getLiquid();
		return fuel != null && fuel.amount > 0 && penaltyCooling == 0 && tile.isRedstonePowered;
	}

	@Override
	public void burn() {
		currentOutput = 0;
		LiquidStack fuel = this.fuelTank.getLiquid();
		if(currentFuel == null) {
			currentFuel = IronEngineFuel.getFuelForLiquid(fuel);
		}

		if (currentFuel == null)
			return;

		if (penaltyCooling <= 0 && tile.isRedstonePowered) {

			lastPowered = true;

			if (burnTime > 0 || fuel.amount > 0) {
				if (burnTime > 0) {
					burnTime--;
				} 
				if (burnTime <= 0) {
					if(fuel != null) {
						if (--fuel.amount <= 0) {
							fuelTank.setLiquid(null);
						}
						burnTime = currentFuel.totalBurningTime / LiquidContainerRegistry.BUCKET_VOLUME;
					} else {
						currentFuel = null;
						return;
					}
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
			LiquidStack liquid;
			if (Block.ice.blockID == itemInInventory.itemID && heat > COOLANT_THRESHOLD) {
				liquid = LiquidContainerRegistry.getLiquidForFilledItem(new ItemStack(Item.bucketWater));
			} else {
				liquid = LiquidContainerRegistry.getLiquidForFilledItem(itemInInventory);
			}

			if (liquid != null) {
				if (fill(ForgeDirection.UNKNOWN, liquid, false) == liquid.amount) {
					fill(ForgeDirection.UNKNOWN, liquid, true);
					tile.setInventorySlotContents(0, Utils.consumeItem(itemInInventory));
				}
			}
		}

		if (heat > COOLANT_THRESHOLD) {
			int extraHeat = heat - COOLANT_THRESHOLD;

			LiquidStack coolant = this.coolantTank.getLiquid();
			IronEngineCoolant currentCoolant = IronEngineCoolant.getCoolantForLiquid(coolant);
			if (currentCoolant != null) {
				if (coolant.amount * currentCoolant.coolingPerUnit > extraHeat) {
					coolant.amount -= Math.round(extraHeat / currentCoolant.coolingPerUnit);
					heat = COOLANT_THRESHOLD;
				} else {
					heat -= coolant.amount * currentCoolant.coolingPerUnit;
					coolantTank.setLiquid(null);
				}
			}
		}

		if (heat > 0 && (penaltyCooling > 0 || !tile.isRedstonePowered)) {
			heat -= 10;

		}

		if (heat <= 0) {
			heat = 0;
		}

		if (heat == 0 && penaltyCooling > 0) {
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
		return this.fuelTank.getLiquid() != null ? (int) (((float) this.fuelTank.getLiquid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		if (nbttagcompound.hasKey("liquidId")) {
			fuelTank.setLiquid(new LiquidStack(nbttagcompound.getInteger("liquidId"), nbttagcompound.getInteger("liquidQty"), nbttagcompound
					.getInteger("liquidMeta")));
		} else if (nbttagcompound.hasKey("fuelTank")) {
			fuelTank.setLiquid(LiquidStack.loadLiquidStackFromNBT(nbttagcompound.getCompoundTag("fuelTank")));
		}

		burnTime = nbttagcompound.getInteger("burnTime");

		if (nbttagcompound.hasKey("coolantId")) {
			coolantTank.setLiquid(new LiquidStack(nbttagcompound.getInteger("coolantId"), nbttagcompound.getInteger("coolantQty"), nbttagcompound
					.getInteger("coolantMeta")));
		} else if (nbttagcompound.hasKey("coolantTank")) {
			coolantTank.setLiquid(LiquidStack.loadLiquidStackFromNBT(nbttagcompound.getCompoundTag("coolantTank")));
		}

		heat = nbttagcompound.getInteger("heat");
		penaltyCooling = nbttagcompound.getInteger("penaltyCooling");

		if (nbttagcompound.hasKey("itemInInventory")) {
			NBTTagCompound cpt = nbttagcompound.getCompoundTag("itemInInventory");
			itemInInventory = ItemStack.loadItemStackFromNBT(cpt);
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		if (fuelTank.getLiquid() != null) {
			nbttagcompound.setTag("fuelTank", fuelTank.getLiquid().writeToNBT(new NBTTagCompound()));
		}

		if (coolantTank.getLiquid() != null) {
			nbttagcompound.setTag("coolantTank", coolantTank.getLiquid().writeToNBT(new NBTTagCompound()));
		}

		nbttagcompound.setInteger("burnTime", burnTime);
		nbttagcompound.setInteger("heat", heat);
		nbttagcompound.setInteger("penaltyCooling", penaltyCooling);

		if (itemInInventory != null) {
			NBTTagCompound cpt = new NBTTagCompound();
			itemInInventory.writeToNBT(cpt);
			nbttagcompound.setTag("itemInInventory", cpt);
		}

	}

	public int getScaledCoolant(int i) {
		return coolantTank.getLiquid() != null ? (int) (((float) coolantTank.getLiquid().amount / (float) (MAX_LIQUID)) * i) : 0;
	}

	@Override
	public void delete() {

	}

	@Override
	public void getGUINetworkData(int i, int j) {
		switch (i) {
		case 0:
			int iEnergy = Math.round(energy * 10);
			iEnergy = (iEnergy & 0xffff0000) | (j & 0xffff);
			energy = iEnergy / 10;
			break;
		case 1:
			iEnergy = Math.round(energy * 10);
			iEnergy = (iEnergy & 0xffff) | ((j & 0xffff) << 16);
			energy = iEnergy / 10;
			break;
		case 2:
			currentOutput = j / 10;
			break;
		case 3:
			heat = (heat & 0xffff0000) | (j & 0xffff);
			break;
		case 4:
			heat = (heat & 0xffff) | ((j & 0xffff) << 16);
			break;
		case 5:
			if (fuelTank.getLiquid() == null) {
				fuelTank.setLiquid(new LiquidStack(0, j));
			} else {
				fuelTank.getLiquid().amount = j;
			}
			break;
		case 6:
			if (fuelTank.getLiquid() == null) {
				fuelTank.setLiquid(new LiquidStack(j, 0));
			} else {
				fuelTank.getLiquid().itemID = j;
			}
			break;
		case 7:
			if (coolantTank.getLiquid() == null) {
				coolantTank.setLiquid(new LiquidStack(0, j));
			} else {
				coolantTank.getLiquid().amount = j;
			}
			break;
		case 8:
			if (coolantTank.getLiquid() == null) {
				coolantTank.setLiquid(new LiquidStack(j, 0));
			} else {
				coolantTank.getLiquid().itemID = j;
			}
			break;
		case 9:
			if (fuelTank.getLiquid() == null) {
				fuelTank.setLiquid(new LiquidStack(0, 0, j));
			} else {
				fuelTank.getLiquid().itemMeta = j;
			}
			break;
		case 10:
			if (coolantTank.getLiquid() == null) {
				coolantTank.setLiquid(new LiquidStack(0, 0, j));
			} else {
				coolantTank.getLiquid().itemMeta = j;
			}
		}
	}

	@Override
	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		iCrafting.sendProgressBarUpdate(containerEngine, 0, Math.round(energy * 10) & 0xffff);
		iCrafting.sendProgressBarUpdate(containerEngine, 1, (Math.round(energy * 10) & 0xffff0000) >> 16);
		iCrafting.sendProgressBarUpdate(containerEngine, 2, Math.round(currentOutput * 10));
		iCrafting.sendProgressBarUpdate(containerEngine, 3, heat & 0xffff);
		iCrafting.sendProgressBarUpdate(containerEngine, 4, (heat & 0xffff0000) >> 16);
		iCrafting.sendProgressBarUpdate(containerEngine, 5, fuelTank.getLiquid() != null ? fuelTank.getLiquid().amount : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 6, fuelTank.getLiquid() != null ? fuelTank.getLiquid().itemID : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 7, coolantTank.getLiquid() != null ? coolantTank.getLiquid().amount : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 8, coolantTank.getLiquid() != null ? coolantTank.getLiquid().itemID : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 9, fuelTank.getLiquid() != null ? fuelTank.getLiquid().itemMeta : 0);
		iCrafting.sendProgressBarUpdate(containerEngine, 10, coolantTank.getLiquid() != null ? coolantTank.getLiquid().itemMeta : 0);
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
	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {

		// Handle coolant
		if (IronEngineCoolant.getCoolantForLiquid(resource) != null)
			return fillCoolant(from, resource, doFill);

		if (IronEngineFuel.getFuelForLiquid(resource) != null)
			return fuelTank.fill(resource, doFill);

		return 0;
	}

	private int fillCoolant(ForgeDirection from, LiquidStack resource, boolean doFill) {
		return coolantTank.fill(resource, doFill);
	}

	@Override
	public LiquidTank[] getLiquidSlots() {
		return new LiquidTank[] { fuelTank, coolantTank };
	}

	/* IINVENTORY */
	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return itemInInventory;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		itemInInventory = itemstack;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if (itemInInventory != null) {
			if (itemInInventory.stackSize <= 0) {
				itemInInventory = null;
				return null;
			}
			ItemStack newStack = itemInInventory;
			if (amount >= newStack.stackSize) {
				itemInInventory = null;
			} else {
				newStack = itemInInventory.splitStack(amount);
			}

			return newStack;
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (itemInInventory == null)
			return null;
		ItemStack toReturn = itemInInventory;
		itemInInventory = null;
		return toReturn;
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		switch (direction) {
		case UP:
			return fuelTank;
		case DOWN:
			return coolantTank;
		default:
			return null;
		}
	}

	public int getFuelId() {
		return fuelTank.getLiquid() != null ? fuelTank.getLiquid().itemID : 0;
	}

	public int getFuelMeta() {
		return fuelTank.getLiquid() != null ? fuelTank.getLiquid().itemMeta : 0;
	}

	public int getCoolantId() {
		return coolantTank.getLiquid() != null ? coolantTank.getLiquid().itemID : 0;
	}

	public int getCoolantMeta() {
		return coolantTank.getLiquid() != null ? coolantTank.getLiquid().itemMeta : 0;
	}
}
