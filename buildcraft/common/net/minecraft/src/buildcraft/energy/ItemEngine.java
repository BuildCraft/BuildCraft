package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.ItemBlock;

public class ItemEngine extends ItemBlock
{

    public ItemEngine(int i)
    {
        super(i);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    public int getPlacedBlockMetadata(int i)
    {
        return i;
    }

//    public String getItemNameIS(ItemStack itemstack)
//    {
//    	if (itemstack.getItemDamage() == 0) {
//    		return "tile.engineWood";
//    	} else {
//    		return "tile.engineStone";
//    	}
////        return (new StringBuilder()).append(super.getItemName()).append(".").append(ItemDye.dyeColors[BlockCloth.func_21034_c(itemstack.getItemDamage())]).toString();
//    }
}
