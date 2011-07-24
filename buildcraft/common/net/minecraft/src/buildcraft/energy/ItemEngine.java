package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;

public class ItemEngine extends ItemBlock
{

    public ItemEngine(int i)
    {
        super(i);
        setMaxDamage(0);
        setHasSubtypes(true);
    }
    
    public int getMetadata(int i)
    {
        return getPlacedBlockMetadata(i);
    }

    public int getPlacedBlockMetadata(int i)
    {
        return i;
    }

    public String getItemNameIS(ItemStack itemstack)
    {
    	if (itemstack.getItemDamage() == 0) {
    		return "tile.engineWood";
    	} else if (itemstack.getItemDamage() == 1) {
    		return "tile.engineStone";
    	} else {
    		return "tile.engineIron";
    	}
    }
}
