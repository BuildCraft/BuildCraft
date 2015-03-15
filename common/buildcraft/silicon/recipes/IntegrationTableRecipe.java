/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.recipes;

import net.minecraft.item.ItemStack;

import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IIntegrationRecipe;
import buildcraft.core.recipes.FlexibleRecipe;
import buildcraft.silicon.TileIntegrationTable;

public abstract class IntegrationTableRecipe extends FlexibleRecipe<ItemStack> implements IIntegrationRecipe {

	@Override
	public final CraftingResult<ItemStack> craft(IFlexibleCrafter crafter, boolean preview) {
		TileIntegrationTable table = (TileIntegrationTable) crafter;

		ItemStack inputA;
		ItemStack inputB;

		if (preview) {
			inputA = table.getStackInSlot(TileIntegrationTable.SLOT_INPUT_A);
			inputB = table.getStackInSlot(TileIntegrationTable.SLOT_INPUT_B);

			if (inputA != null) {
				inputA = inputA.copy();
			}

			if (inputB != null) {
				inputB = inputB.copy();
			}
		} else {
			inputA = table.decrStackSize(TileIntegrationTable.SLOT_INPUT_A, 1);
			inputB = table.decrStackSize(TileIntegrationTable.SLOT_INPUT_B, 1);
		}

		return craft(table, preview, inputA, inputB);
	}

	public CraftingResult<ItemStack> craft(TileIntegrationTable crafter, boolean preview, ItemStack inputA,
			ItemStack inputB) {
		return super.craft(crafter, preview);
	}

	@Override
	public CraftingResult canCraft(ItemStack expectedOutput) {
		return null;
	}
}
