/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.boards;

import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.core.recipes.FlexibleRecipe;
import buildcraft.core.utils.NBTUtils;

public class BoardRecipe extends FlexibleRecipe {

	public BoardRecipe(String id) {
		ItemStack output = new ItemStack(BuildCraftSilicon.redstoneBoard);
		NBTUtils.getItemData(output).setString("id", "<unknown>");

		setContents(id, output, 1000, 0, new ItemStack(BuildCraftSilicon.redstoneBoard));
	}

	@Override
	public CraftingResult<ItemStack> craft(IFlexibleCrafter crafter, boolean preview) {
		CraftingResult<ItemStack> result = super.craft(crafter, preview);

		if (result != null) {
			ItemStack stack = new ItemStack(BuildCraftSilicon.redstoneBoard);
			RedstoneBoardRegistry.instance.createRandomBoard(NBTUtils.getItemData(stack));

			result.crafted = stack;

			return result;
		} else {
			return null;
		}
	}
}
