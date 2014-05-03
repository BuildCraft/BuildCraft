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

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.ItemFacade;

public enum CreativeTabBuildCraft {

	BLOCKS,
	ITEMS,
	PIPES,
	FACADES;
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
			return new ItemStack (BuildCraftFactory.quarryBlock, 1);
		case ITEMS:
			return new ItemStack (BuildCraftCore.wrenchItem, 1);
		case PIPES:
			return new ItemStack (BuildCraftTransport.pipeItemsDiamond, 1);
		case FACADES:
			return ItemFacade.getFacade(Blocks.brick_block, 0);
		}

		return ItemFacade.getFacade(Blocks.brick_block, 0);
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
