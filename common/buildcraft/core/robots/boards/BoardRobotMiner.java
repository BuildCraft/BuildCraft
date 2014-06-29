/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;

public class BoardRobotMiner extends BoardRobotGenericBreakBlock {

	private boolean extendedOre = false;

	public BoardRobotMiner(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		super.delegateAIEnded(ai);

		if (ai instanceof AIRobotFetchAndEquipItemStack) {
			ItemStack stack = robot.getHeldItem();

			if (stack != null && stack.getItem() instanceof ItemPickaxe) {
				ItemPickaxe pickaxe = (ItemPickaxe) stack.getItem();

				extendedOre = pickaxe.func_150913_i() == Item.ToolMaterial.EMERALD
						|| pickaxe.func_150913_i() == Item.ToolMaterial.IRON;
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotMinerNBT.instance;
	}

	@Override
	public boolean isExpectedTool(ItemStack stack) {
		return stack.getItem() instanceof ItemPickaxe;
	}

	@Override
	public boolean isExpectedBlock(World world, int x, int y, int z) {
		if (!extendedOre) {
			return BuildCraftAPI.isBasicOreProperty.get(world, x, y, z);
		} else {
			return BuildCraftAPI.isExtendedOreProperty.get(world, x, y, z);
		}
	}

}
