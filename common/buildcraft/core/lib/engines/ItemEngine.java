/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.engines;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.items.ItemBlockBuildCraft;

public class ItemEngine extends ItemBlockBuildCraft {
	private final BlockEngineBase engineBlock;

	public ItemEngine(Block block) {
		super(block);
		engineBlock = (BlockEngineBase) block;
		setCreativeTab(BCCreativeTab.get("main"));
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return engineBlock.getUnlocalizedName(itemstack.getItemDamage());
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		return null;
	}
}
