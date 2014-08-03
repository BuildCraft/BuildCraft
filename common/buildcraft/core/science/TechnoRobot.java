/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.science;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.ItemRobot;
import buildcraft.core.utils.NBTUtils;

public class TechnoRobot extends Technology {

	ItemStack robotItem;
	RedstoneBoardRobotNBT robot;

	public void initialize(Tier iTier,
			RedstoneBoardRobotNBT robot,
			ItemStack requirement,
			Technology... iPrerequisites) {
		initialize(iTier, robot, requirement, null, null, iPrerequisites);
	}

	public void initialize(Tier iTier,
			RedstoneBoardRobotNBT robot,
			ItemStack requirement1,
			ItemStack requirement2,
			Technology... iPrerequisites) {
		initialize(iTier, robot, requirement1, requirement2, null, iPrerequisites);
	}

	public void initialize(Tier iTier,
			RedstoneBoardRobotNBT iRobot,
			ItemStack requirement1,
			ItemStack requirement2,
			ItemStack requirement3,
			Technology... iPrerequisites) {

		super.initialize("robot:" + iRobot.getID(),
				iTier, requirement1, requirement2, requirement3, iPrerequisites);

		robot = iRobot;
		ItemStack robotStack = new ItemStack(BuildCraftSilicon.robotItem);
		NBTTagCompound nbt = NBTUtils.getItemData(robotStack);
		robot.createBoard(nbt);
		robotItem = ItemRobot.createRobotStack(robotStack);
	}

	@Override
	public ItemStack getStackToDisplay() {
		return robotItem;
	}
}
