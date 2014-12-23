/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsWood extends WorldProperty {

	private int woodId = 0;

	public WorldPropertyIsWood() {
		woodId = OreDictionary.getOreID("logWood");
	}

	@Override
	public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
		ItemStack stack = Utils.getItemStack(state);

		if (stack.getItem() != null) {
			for (int id : OreDictionary.getOreIDs(stack)) {
				if (id == woodId) {
					return true;
				}
			}
		}

		return false;
	}
}
