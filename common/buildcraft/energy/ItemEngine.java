/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBlockBuildCraft;

public class ItemEngine extends ItemBlockBuildCraft {

	public ItemEngine(Block block) {
		super(block);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		switch (itemstack.getItemDamage()) {
			case 0:
				return "tile.engineWood";
			case 1:
				return "tile.engineStone";
			case 2:
				return "tile.engineIron";
			case 3:
				return "tile.engineCreative";
			default:
				return "tile.engineWood";
		}
	}
}
