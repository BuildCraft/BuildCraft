/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.recipes.IntegrationRecipeBC;

public class RobotIntegrationRecipe extends IntegrationRecipeBC {
	public RobotIntegrationRecipe() {
		super(50000, 1);
	}

	@Override
	public List<ItemStack> generateExampleInput() {
		ArrayList<ItemStack> example = new ArrayList<ItemStack>();
		example.add(ItemRobot.createRobotStack(RedstoneBoardRegistry.instance.getEmptyRobotBoard(), 0));
		return example;
	}

	@Override
	public List<List<ItemStack>> generateExampleExpansions() {
		ArrayList<List<ItemStack>> list = new ArrayList<List<ItemStack>>();
		ArrayList<ItemStack> example = new ArrayList<ItemStack>();
		for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
			ItemStack stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
			nbt.createBoard(NBTUtils.getItemData(stack));
			example.add(stack);
		}
		list.add(example);
		return list;
	}

	@Override
	public List<ItemStack> generateExampleOutput() {
		ArrayList<ItemStack> example = new ArrayList<ItemStack>();
		for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
			example.add(ItemRobot.createRobotStack((RedstoneBoardRobotNBT) nbt, 0));
		}
		return example;
	}

	@Override
	public boolean isValidInput(ItemStack input) {
		return input.getItem() instanceof ItemRobot;
	}

	@Override
	public boolean isValidExpansion(ItemStack input, ItemStack expansion) {
		return expansion.getItem() instanceof ItemRedstoneBoard;
	}

	@Override
	public ItemStack craft(ItemStack input, List<ItemStack> expansions, boolean preview) {
		if (!preview) {
			expansions.get(0).stackSize--;
		}
		RedstoneBoardRobotNBT boardNBT = (RedstoneBoardRobotNBT) ItemRedstoneBoard.getBoardNBT(expansions.get(0));

		int energy = ItemRobot.getEnergy(input);
		if (energy == 0) {
			energy = EntityRobotBase.SAFETY_ENERGY;
		}
		return ItemRobot.createRobotStack(boardNBT, energy);
	}
}
