/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.properties;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsRock extends WorldProperty {

	private final HashSet<Integer> rockIds = new HashSet<Integer>();

	public WorldPropertyIsRock() {
		rockIds.add(OreDictionary.getOreID("stone"));
		rockIds.add(OreDictionary.getOreID("cobblestone"));
		rockIds.add(OreDictionary.getOreID("sandstone"));
	}

	@Override
	public boolean get(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
		if (block == null) {
			return false;
		} else if (block == Blocks.stone) {
			return true;
		} else {
			ItemStack stack = new ItemStack(block);

			if (stack.getItem() != null) {
				for (int id : OreDictionary.getOreIDs(stack)) {
					if (rockIds.contains(id)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
