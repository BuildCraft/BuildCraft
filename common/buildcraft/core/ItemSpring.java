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

import buildcraft.api.enums.EnumSpring;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemSpring extends ItemBlockBuildCraft {

	public ItemSpring(Block block) {
		super(block);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "tile.spring." + EnumSpring.values()[(stack.getItemDamage())].name().toLowerCase(Locale.ENGLISH);
	}
}
