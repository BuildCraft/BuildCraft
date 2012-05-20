package net.minecraft.src.buildcraft.core;

import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.utils.StringUtil;

public class ItemBlockBuildCraft extends ItemBlock {

	protected String name;
	
	public ItemBlockBuildCraft(int id, String name) {
		super(id);
		this.name = name;
	}

    @Override
    public int getMetadata(int i) {
        return i;
    }

	//@Override Client side only
    public String getItemDisplayName(ItemStack itemstack) {
    	return StringUtil.localize(getItemNameIS(itemstack));
    }

}
