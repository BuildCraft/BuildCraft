/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;
import buildcraft.energy.gui.ContainerEngine;

public class EngineStone extends Engine {

	int burnTime = 0;
	int totalBurnTime = 0;

	private ItemStack itemInInventory;

	public EngineStone(TileEngine engine) {
		super(engine);

		maxEnergy = 10000;
		maxEnergyExtracted = 100;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_PATH_BLOCKS + "/base_stone.png";
	}

	@Override
	public int explosionRange() {
		return 4;
	}

	@Override
	public int maxEnergyReceived() {
		return 200;
	}

	@Override
	public float getPistonSpeed() {
		switch (getEnergyStage()) {
		case Blue:
			return 0.02F;
		case Green:
			return 0.04F;
		case Yellow:
			return 0.08F;
		case Red:
			return 0.16F;
		default:
			return 0;
		}
	}

	@Override
	public boolean isBurning() {
		return burnTime > 0;
	}

	@Override
	public void burn() {
		currentOutput = 0;
		if (burnTime > 0) {
			burnTime--;
			currentOutput = 1;
			addEnergy(1);
		}

		if (burnTime == 0 && tile.isRedstonePowered) {

			burnTime = totalBurnTime = getItemBurnTime(tile.getStackInSlot(0));

			if (burnTime > 0) {
				tile.setInventorySlotContents(0, Utils.consumeItem(tile.getStackInSlot(0)));
			}
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) burnTime / (float) totalBurnTime) * i);
	}

	private int getItemBurnTime(ItemStack itemstack) {
		if (itemstack == null)
			return 0;

		return TileEntityFurnace.getItemBurnTime(itemstack);
	}

	/* SAVING & LOADING */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		burnTime = nbttagcompound.getInteger("burnTime");
		totalBurnTime = nbttagcompound.getInteger("totalBurnTime");

		if (nbttagcompound.hasKey("itemInInventory")) {
			NBTTagCompound cpt = nbttagcompound.getCompoundTag("itemInInventory");
			itemInInventory = ItemStack.loadItemStackFromNBT(cpt);
		}

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("burnTime", burnTime);
		nbttagcompound.setInteger("totalBurnTime", totalBurnTime);

		if (itemInInventory != null) {
			NBTTagCompound cpt = new NBTTagCompound();
			itemInInventory.writeToNBT(cpt);
			nbttagcompound.setTag("itemInInventory", cpt);
		}

	}

	@Override
	public void delete() {
		ItemStack stack = tile.getStackInSlot(0);
		if (stack != null) {
			Utils.dropItems(tile.worldObj, stack, tile.xCoord, tile.yCoord, tile.zCoord);
		}
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
			burnTime = j;
			break;
		case 3:
			totalBurnTime = j;
			break;
		}
	}

	@Override
	public void sendGUINetworkData(ContainerEngine containerEngine, ICrafting iCrafting) {
		iCrafting.sendProgressBarUpdate(containerEngine, 0, Math.round(energy));
		iCrafting.sendProgressBarUpdate(containerEngine, 1, Math.round(currentOutput));
		iCrafting.sendProgressBarUpdate(containerEngine, 2, burnTime);
		iCrafting.sendProgressBarUpdate(containerEngine, 3, totalBurnTime);
	}

	@Override
	public int getHeat() {
		return Math.round(energy);
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
		return null;
	}
}
