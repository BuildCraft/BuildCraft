package net.minecraft.src.buildcraft;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;


public class ItemWoodGear extends Item {

	public ItemWoodGear(int i) {
		super(i);		
	}
	
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        itemstack.stackSize--;
        
        return itemstack;
    }

}
