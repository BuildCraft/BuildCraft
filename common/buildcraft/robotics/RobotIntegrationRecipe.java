/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.core.recipes.IntegrationRecipeBC;

public class RobotIntegrationRecipe extends IntegrationRecipeBC {
	public RobotIntegrationRecipe() {
		super(50000, 1);
	}

	@Override
	public List<ItemStack> generateExampleInput() {
		ArrayList<ItemStack> example = new ArrayList<ItemStack>();
		example.add(ItemRobot.createRobotStack(null, 0));
		return example;
	}

	@Override
	public List<List<ItemStack>> generateExampleExpansions() {
		// TODO!
		return null;
	}

	@Override
	public boolean isValidInput(ItemStack input) {
		return input.getItem() instanceof ItemRobot;
	}

	@Override
	public boolean isValidExpansion(ItemStack expansion) {
		return expansion.getItem() instanceof ItemRedstoneBoard;
	}

	@Override
	public ItemStack craft(ItemStack input, List<ItemStack> expansions, boolean preview) {
		if (!preview) {
			expansions.get(0).stackSize--;
		}
		return ItemRobot.createRobotStack(expansions.get(0), 0);
	}
}
