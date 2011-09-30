/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;

public class EngineStone extends Engine {

	int burnTime = 0;
	int totalBurnTime = 0;
	
	public EngineStone(TileEngine engine) {
		super(engine);
		
		maxEnergy = 10000;
		maxEnergyExtracted = 100;
	}
	
	public String getTextureFile () {
		return "/net/minecraft/src/buildcraft/energy/gui/base_stone.png";
	}
	
	public int explosionRange () {
		return 4;
	}
	
	public int maxEnergyReceived () {
		return 200;
	}
	
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
	
	public boolean isBurning () {
		return burnTime > 0;
	}
	
	public void burn () {
		if(burnTime > 0) {
			burnTime--;
			addEnergy(1);
		}

		if (burnTime == 0 && tile.worldObj.isBlockIndirectlyGettingPowered(tile.xCoord,
				tile.yCoord, tile.zCoord)) {
			
			
			burnTime = totalBurnTime = getItemBurnTime(tile.getStackInSlot(0));
			
			if (burnTime > 0) {
				tile.decrStackSize(1, 1);				
			}
		}
	}

	@Override
	public int getScaledBurnTime(int i) {
		return (int) ((float) burnTime / (float) totalBurnTime) * i;
	}

	private int getItemBurnTime(ItemStack itemstack)
    {
        if(itemstack == null)
        {
            return 0;
        }
        int i = itemstack.getItem().shiftedIndex;
        if(i < 256 && Block.blocksList[i].blockMaterial == Material.wood)
        {
            return 300;
        }
        if(i == Item.stick.shiftedIndex)
        {
            return 100;
        }
        if(i == Item.coal.shiftedIndex)
        {
            return 1600;
        }
        if(i == Item.bucketLava.shiftedIndex)
        {
            return 20000;
        } else
        {
            return i == Block.sapling.blockID ? 100 : ModLoader.AddAllFuel(i, itemstack.getItemDamage());
        }
    }
}
