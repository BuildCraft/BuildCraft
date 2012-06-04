/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.ItemBucket;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.core.utils.StringUtil;
import net.minecraft.src.forge.ITextureProvider;

public class ItemBucketOil extends ItemBucket implements ITextureProvider {

	public ItemBucketOil(int i) {
		super(i, BuildCraftEnergy.oilMoving.blockID);
		iconIndex = 0 * 16 + 1;
	}

	// @Override Client side only
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.localize(getItemNameIS(itemstack));
	}

	@Override
	public String getTextureFile() {
		return "/net/minecraft/src/buildcraft/core/gui/item_textures.png";
	}

}
