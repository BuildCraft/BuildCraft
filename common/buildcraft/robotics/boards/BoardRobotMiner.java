/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;

public class BoardRobotMiner extends BoardRobotGenericBreakBlock {
	private static final int MAX_HARVEST_LEVEL = 3;
	private int harvestLevel = 0;

	public BoardRobotMiner(EntityRobotBase iRobot) {
		super(iRobot);
		detectHarvestLevel();
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		super.delegateAIEnded(ai);

		if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (ai.success()) {
				detectHarvestLevel();
			}
		}
	}

	private void detectHarvestLevel() {
		ItemStack stack = robot.getHeldItem();

		if (stack != null && stack.getItem() != null && stack.getItem().getToolClasses(stack).contains("pickaxe")) {
			harvestLevel = stack.getItem().getHarvestLevel(stack, "pickaxe");
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BCBoardNBT.REGISTRY.get("miner");
	}

	@Override
	public boolean isExpectedTool(ItemStack stack) {
		return stack != null && stack.getItem().getToolClasses(stack).contains("pickaxe");
	}

	@Override
	public boolean isExpectedBlock(World world, int x, int y, int z) {
		return BuildCraftAPI.getWorldProperty("ore@hardness=" + Math.min(MAX_HARVEST_LEVEL, harvestLevel))
				.get(world, x, y, z);
	}

}
