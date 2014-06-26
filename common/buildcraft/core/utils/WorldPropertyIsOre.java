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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsOre extends WorldProperty {

	public ArrayList<Integer> ores = new ArrayList<Integer>();

	public WorldPropertyIsOre(boolean extendedHarvest) {
		ores.add(OreDictionary.getOreID("oreCoal"));
		ores.add(OreDictionary.getOreID("oreIron"));
		ores.add(OreDictionary.getOreID("oreQuartz"));

		if (extendedHarvest) {
			ores.add(OreDictionary.getOreID("oreGold"));
			ores.add(OreDictionary.getOreID("oreDiamond"));
			ores.add(OreDictionary.getOreID("oreEmerald"));
			ores.add(OreDictionary.getOreID("oreLapis"));
			ores.add(OreDictionary.getOreID("oreRedstone"));
		}
	}

	@Override
	public boolean get(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
		if (block == null) {
			return false;
		} else {
			ItemStack stack = new ItemStack(block);

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
