package buildcraft.core;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.transport.ItemFacade;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public enum CreativeTabBuildCraft {

	TIER_1,
	TIER_2,
	TIER_3,
	TIER_4,
	MISC,
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
			case TIER_1:
				return new ItemStack(BuildCraftCore.woodenGearItem, 1);
			case TIER_2:
				return new ItemStack(BuildCraftEnergy.bucketOil, 1);
			case TIER_3:
				return new ItemStack(BuildCraftCore.redstoneCrystal, 1);
			case TIER_4:
				return new ItemStack(BuildCraftCore.springBlock, 1);
			case MISC:
				return new ItemStack(BuildCraftCore.springBlock, 1);
			case FACADES:
				return ItemFacade.getFacade(Blocks.brick_block, 0);
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
