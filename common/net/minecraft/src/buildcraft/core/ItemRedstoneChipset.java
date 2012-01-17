package net.minecraft.src.buildcraft.core;

import net.minecraft.src.ItemStack;

public class ItemRedstoneChipset extends ItemBuildCraftTexture {

	public ItemRedstoneChipset(int i) {
		super(i);

        setHasSubtypes(true);
        setMaxDamage(0);	
	}

	@Override
	public int getIconFromDamage(int i) {
		switch (i) {
		case 0:
			return 2 * 16 + 2;
		case 1:
			return 2 * 16 + 3;
		case 2:
			return 2 * 16 + 4;
		default:
			return 2 * 16 + 5;
		}
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getItemName()).append(".")
				.append(itemstack.getItemDamage()).toString();
	}

}
