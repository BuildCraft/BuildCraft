package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.ItemBucket;
import net.minecraft.src.forge.ITextureProvider;

public class ItemBucketOil extends ItemBucket implements ITextureProvider {

	public ItemBucketOil(int i) {
		super(i, BuildCraftEnergy.oilMoving.blockID);
		iconIndex = 0 * 16 + 1;
	}

	@Override
	public String getTextureFile() {
		return "/net/minecraft/src/buildcraft/core/gui/item_textures.png";
	}

}
