/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.boards;

import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.EntityRobotBase;

public class BoardRobotLumberjack extends BoardRobotGenericBreakBlock {

	public BoardRobotLumberjack(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public BoardRobotLumberjack(EntityRobotBase iRobot, NBTTagCompound nbt) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotLumberjackNBT.instance;
	}

	@Override
	public boolean isExpectedTool(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemAxe;
	}

	@Override
	public boolean isExpectedBlock(World world, int x, int y, int z) {
		return BuildCraftAPI.isWoodProperty.get(world, x, y, z);
	}
}
