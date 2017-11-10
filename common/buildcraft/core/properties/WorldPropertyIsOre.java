/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsOre extends WorldProperty {
	private final HashSet<Integer> ores = new HashSet<Integer>();

	public WorldPropertyIsOre(int harvestLevel) {
		initBlockHarvestTools();
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

	private void initBlockHarvestTools() {
		// Make sure the static code block in the ForgeHooks class is run
		ForgeHooks.canToolHarvestBlock(Blocks.coal_ore, 0, new ItemStack(Items.diamond_pickaxe));
	}

	@Override
	public boolean get(IBlockAccess blockAccess, Block block, int meta, int x, int y, int z) {
		if (block == null) {
			return false;
		} else {
			List<ItemStack> toCheck = new ArrayList<ItemStack>();
			toCheck.add(new ItemStack(block, 1, meta));

			if (block.hasTileEntity(meta) && blockAccess instanceof World) {
				toCheck.addAll(block.getDrops((World) blockAccess, x, y, z, blockAccess.getBlockMetadata(x, y, z), 0));
			}

			for (ItemStack stack : toCheck) {
				if (stack.getItem() != null) {
					for (int id : OreDictionary.getOreIDs(stack)) {
						if (ores.contains(id)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}
}
