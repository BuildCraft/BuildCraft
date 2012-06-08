/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.utils.StringUtil;
import net.minecraft.src.forge.ITextureProvider;

public class ItemBuildCraft extends Item implements ITextureProvider {

	public ItemBuildCraft(int i) {
		super(i);
	}

	// @Override Client side only
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.localize(getItemNameIS(itemstack));
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_ITEMS;
	}

}
