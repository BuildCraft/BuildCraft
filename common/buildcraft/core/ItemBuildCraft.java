/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.core.utils.StringUtil;

public class ItemBuildCraft extends Item {

	public ItemBuildCraft(int i) {
		super(i);
		setTextureFile(DefaultProps.TEXTURE_ITEMS);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.localize(getItemNameIS(itemstack));
	}
}
