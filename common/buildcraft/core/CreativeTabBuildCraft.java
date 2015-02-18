/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;

public enum CreativeTabBuildCraft {

	BLOCKS,
	ITEMS,
	PIPES,
	FACADES,
	BOARDS;
	private final CreativeTabs tab;

	private CreativeTabBuildCraft() {
		tab = new Tab();
	}

	public CreativeTabs get() {
		return tab;
	}

	private String getLabel() {
		return "buildcraft." + name().toLowerCase(Locale.ENGLISH);
	}

	private ItemStack getItem() {
		switch (this) {
		case BLOCKS:
			return getAvailableItem(BuildCraftFactory.quarryBlock, BuildCraftCore.ironGearItem);
		case ITEMS:
			return new ItemStack(BuildCraftCore.wrenchItem, 1);
		case PIPES:
			return getAvailableItem(BuildCraftTransport.pipeItemsDiamond, BuildCraftCore.stoneGearItem);
		case FACADES:
			return BuildCraftTransport.facadeItem != null ? BuildCraftTransport.facadeItem.getFacadeForBlock(Blocks.brick_block, 0)
					: new ItemStack(BuildCraftCore.woodenGearItem, 1);
		case BOARDS:
			return getAvailableItem(BuildCraftSilicon.redstoneBoard, BuildCraftCore.diamondGearItem);
		}

		return new ItemStack(BuildCraftCore.wrenchItem, 1);
	}

	private ItemStack getAvailableItem(Object... items) {
		for (Object item : items) {
			if (item == null) {
				continue;
			}
			if (item instanceof Item) {
				return new ItemStack((Item) item, 1);
			}
			if (item instanceof Block) {
				return new ItemStack((Block) item, 1);
			}
			throw new IllegalArgumentException();
		}
		return null;
	}

	private final class Tab extends CreativeTabs {

		private Tab() {
			super(getLabel());
		}

		@Override
		public ItemStack getIconItemStack() {
			return getItem();
		}

		@Override
		public Item getTabIconItem() {
			return getItem().getItem();
		}
	}
}
