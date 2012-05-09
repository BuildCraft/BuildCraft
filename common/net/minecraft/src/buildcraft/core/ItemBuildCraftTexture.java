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
import net.minecraft.src.forge.ITextureProvider;

public class ItemBuildCraftTexture extends Item implements ITextureProvider {

	public ItemBuildCraftTexture(int i) {
		super(i);
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftSprites;
	}

}
