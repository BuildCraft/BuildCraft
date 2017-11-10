/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BCCreativeTab extends CreativeTabs {
	private static final Map<String, BCCreativeTab> tabs = new HashMap<String, BCCreativeTab>();

	private ItemStack icon;

	public BCCreativeTab(String name) {
		super("buildcraft." + name);

		tabs.put(name, this);
	}

	public static BCCreativeTab get(String name) {
		return tabs.get(name);
	}

	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}

	private ItemStack getItem() {
		if (icon == null || icon.getItem() == null) {
			return new ItemStack(Blocks.brick_block, 1);
		}
		return icon;
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
