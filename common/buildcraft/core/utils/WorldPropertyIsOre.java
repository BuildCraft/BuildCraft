/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsOre extends WorldProperty {

	public HashSet<Integer> ores = new HashSet<Integer>();

	public WorldPropertyIsOre(int harvestLevel) {
		for (String oreName : OreDictionary.getOreNames()) {
			if (oreName.startsWith("ore")) {
				List<ItemStack> oreStacks = OreDictionary.getOres(oreName);
				if (oreStacks.size() > 0) {
					Block block = Block.getBlockFromItem(oreStacks.get(0).getItem());
					try {
						IBlockState state = block.getStateFromMeta(oreStacks.get(0).getItemDamage());
						if ("pickaxe".equals(block.getHarvestTool(state)) &&
								block.getHarvestLevel(state) <= harvestLevel) {
							ores.add(OreDictionary.getOreID(oreName));
						}
					} catch(Exception e) {

					}
				}
			}
		}
	}

	@Override
	public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
		ItemStack stack = Utils.getItemStack(state);

		if (stack.getItem() != null) {
			for (int id : OreDictionary.getOreIDs(stack)) {
				if (ores.contains(id)) {
					return true;
				}
			}
		}

		return false;
	}
}
