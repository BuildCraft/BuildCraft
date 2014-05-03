package buildcraft.core;

import java.util.Locale;

import buildcraft.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.transport.ItemFacade;

public enum CreativeTabBuildCraft {

	BLOCKS,
	ITEMS,
	PIPES;
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
		}

		return ItemFacade.getFacade(Blocks.brick_block, 0);
	}

	private class Tab extends CreativeTabs {

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
