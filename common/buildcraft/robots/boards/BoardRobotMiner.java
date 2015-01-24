/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.boards;

import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robots.ai.AIRobotFetchAndEquipItemStack;

public class BoardRobotMiner extends BoardRobotGenericBreakBlock {

	private int harvestLevel = 0;

	public BoardRobotMiner(EntityRobotBase iRobot) {
		super(iRobot);
		detectHarvestLevel();
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		super.delegateAIEnded(ai);

		if (ai instanceof AIRobotFetchAndEquipItemStack) {
			detectHarvestLevel();
		}
	}

	private void detectHarvestLevel() {
		ItemStack stack = robot.getHeldItem();

		if (stack != null && stack.getItem() instanceof ItemPickaxe) {
			ItemPickaxe pickaxe = (ItemPickaxe) stack.getItem();

			harvestLevel = pickaxe.getHarvestLevel(stack, "pickaxe");
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotMinerNBT.instance;
	}

	@Override
	public boolean isExpectedTool(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemPickaxe;
	}

	@Override
	public boolean isExpectedBlock(World world, int x, int y, int z) {
		return BuildCraftAPI.isOreProperty[Math.min(BuildCraftAPI.isOreProperty.length, harvestLevel)]
				.get(world, x, y, z);
	}

}
