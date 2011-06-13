package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

public class ItemTemplate extends Item {

	public ItemTemplate(int i) {
		super(i);
		
		maxStackSize = 1;
		
		iconIndex = ModLoader.addOverride("/gui/items.png",
				"/net/minecraft/src/buildcraft/builders/gui/template.png");
	}

    public String getItemNameIS(ItemStack itemstack) {
        return getItemName() + "#" + itemstack.getItemDamage();
    }
    
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {
    }
    
}
