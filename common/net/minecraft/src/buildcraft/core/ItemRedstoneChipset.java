package net.minecraft.src.buildcraft.core;

import java.util.ArrayList;

import net.minecraft.src.ItemStack;

public class ItemRedstoneChipset extends ItemBuildCraftTexture {

	public ItemRedstoneChipset(int i) {
		super(i);

        setHasSubtypes(true);
        setMaxDamage(0);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public int getIconFromDamage(int i) {
		switch (i) {
		case 0:
			return 6 * 16 + 0;
		case 1:
			return 6 * 16 + 1;
		case 2:
			return 6 * 16 + 2;
		case 3:
			return 6 * 16 + 3;
		default:
			return 6 * 16 + 4;
		}
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getItemName()).append(".")
				.append(itemstack.getItemDamage()).toString();
	}

    @Override
    public void addCreativeItems(ArrayList itemList) {
    	for(int i = 0; i < 5; i++)
    		itemList.add(new ItemStack(this, 1, i));
    }
}
