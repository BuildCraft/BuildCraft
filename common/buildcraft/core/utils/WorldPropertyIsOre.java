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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsOre extends WorldProperty {

	public HashSet<Integer> ores = new HashSet<Integer>();

	public WorldPropertyIsOre(int harvestLevel) {
		for (String oreName : OreDictionary.getOreNames()) {
			if (oreName.startsWith("ore")) {
				ArrayList<ItemStack> oreStacks = OreDictionary.getOres(oreName);
				if (oreStacks.size() > 0) {
					Block block = Block.getBlockFromItem(oreStacks.get(0).getItem());
					int meta = oreStacks.get(0).getItemDamage();
					if (meta >= 16 || meta < 0) {
						meta = 0;
					}
					if (block == null) {
						continue;
					}
					if ("pickaxe".equals(block.getHarvestTool(meta)) &&
							block.getHarvestLevel(meta) <= harvestLevel) {
						ores.add(OreDictionary.getOreID(oreName));
					}
				}
			}
		}
	}

	@Override
	public boolean get(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
		if (block == null) {
			return false;
		} else {
			ItemStack stack = new ItemStack(block, 1, meta);

			if (stack.getItem() != null) {
				for (int id : OreDictionary.getOreIDs(stack)) {
					if (ores.contains(id)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
