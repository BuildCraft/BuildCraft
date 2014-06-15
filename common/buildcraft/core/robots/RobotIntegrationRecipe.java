/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.core.ItemRobot;
import buildcraft.core.utils.NBTUtils;
import buildcraft.silicon.ItemRedstoneBoard;
import buildcraft.silicon.TileIntegrationTable;
import buildcraft.silicon.recipes.IntegrationTableRecipe;

public class RobotIntegrationRecipe extends IntegrationTableRecipe {

	public RobotIntegrationRecipe(String id) {
		setContents(id, new ItemStack(BuildCraftSilicon.robotItem), 10000, 0);
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		return inputA != null && inputA.getItem() instanceof ItemRobot;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return inputB != null && inputB.getItem() instanceof ItemRedstoneBoard;
	}

	@Override
	public CraftingResult<ItemStack> craft(TileIntegrationTable crafter, boolean preview, ItemStack inputA,
			ItemStack inputB) {
		CraftingResult<ItemStack> result = super.craft(crafter, preview, inputA, inputB);

		if (result != null) {
			ItemStack robot = new ItemStack(BuildCraftSilicon.robotItem);

			NBTUtils.getItemData(robot).setTag("board", NBTUtils.getItemData(inputB));

			result.crafted = robot;

			return result;
		} else {
			return null;
		}
	}
}
