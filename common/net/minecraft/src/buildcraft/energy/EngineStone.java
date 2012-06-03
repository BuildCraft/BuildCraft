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
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;

public class EngineStone extends Engine {

	int burnTime = 0;
	int totalBurnTime = 0;
	
	public EngineStone(TileEngine engine) {
		super(engine);
		
		maxEnergy = 10000;
		maxEnergyExtracted = 100;
	}
	
	@Override
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_stone.png";
	}
	
	@Override
	public int explosionRange () {
		return 4;
	}
	
	@Override
	public int maxEnergyReceived () {
		return 200;
	}
	
	@Override
	public float getPistonSpeed () {
		switch (getEnergyStage()) {
		case Blue:
			return 0.02F;
		case Green:
			return 0.04F;
		case Yellow:
			return 0.08F;
		case Red:
			return 0.16F;
		}
		
		return 0;
	}
	
	@Override
	public boolean isBurning () {
		return burnTime > 0;
	}
	
	@Override
	public void burn () {
		currentOutput = 0;
		if(burnTime > 0) {
			burnTime--;
			currentOutput = 1;
			addEnergy(1);
		}

		if (burnTime == 0 && tile.isRedstonePowered) {
			
			
			burnTime = totalBurnTime = getItemBurnTime(tile.getStackInSlot(0));
			
			if (burnTime > 0) {
				tile.setInventorySlotContents(0,
						Utils.consumeItem(tile.getStackInSlot(0)));
			}
		}
	}
	
	@Override
	public int getScaledBurnTime(int i) {
		return (int) (((float) burnTime / (float) totalBurnTime) * i);
	}

	private int getItemBurnTime(ItemStack itemstack) {
		if (itemstack == null) {
			return 0;
		}
		int i = itemstack.getItem().shiftedIndex;
		if (i < Block.blocksList.length
				&& Block.blocksList[i] != null
				&& Block.blocksList[i].blockMaterial == Material.wood) {
			return 300;
		}
		if (i == Item.stick.shiftedIndex) {
			return 100;
		}
		if (i == Item.coal.shiftedIndex) {
			return 1600;
		}
		if (i == Item.bucketLava.shiftedIndex) {
			return 20000;
		} else {
			return i == Block.sapling.blockID ? 100 : CoreProxy.addFuel(i,
					itemstack.getItemDamage());
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		burnTime = nbttagcompound.getInteger("burnTime");
		totalBurnTime = nbttagcompound.getInteger("totalBurnTime");
    }
    
	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	nbttagcompound.setInteger("burnTime", burnTime);
		nbttagcompound.setInteger("totalBurnTime", totalBurnTime);
    }
	
	@Override
	public void delete()
	{
		ItemStack stack = tile.getStackInSlot(0);
		if(stack != null)
			Utils.dropItems(tile.worldObj, stack, tile.xCoord, tile.yCoord, tile.zCoord);
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
	public void sendGUINetworkData(ContainerEngine containerEngine,
			ICrafting iCrafting) {
		iCrafting.updateCraftingInventoryInfo(containerEngine, 0, energy);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 1, currentOutput);	
		iCrafting.updateCraftingInventoryInfo(containerEngine, 2, burnTime);
		iCrafting.updateCraftingInventoryInfo(containerEngine, 3, totalBurnTime);	
	}
	
	@Override public int getHeat() { return energy; }
}
