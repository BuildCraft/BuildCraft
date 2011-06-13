package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;

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
}
