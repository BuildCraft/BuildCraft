package net.minecraft.src.buildcraft.core;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Item;
import net.minecraft.src.forge.ITextureProvider;

public class BuildCraftItem extends Item implements ITextureProvider {

	public BuildCraftItem(int i) {
		super(i);
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftSprites;
	}

}
