package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Item;
import net.minecraft.src.forge.ITextureProvider;

public class ItemFuel extends Item implements ITextureProvider {

	public ItemFuel(int i) {
		super(i);
		iconIndex = 16 * 2 + 0;
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftSprites;
	}

}
