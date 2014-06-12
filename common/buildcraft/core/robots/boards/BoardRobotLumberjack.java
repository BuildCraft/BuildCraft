/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.HashSet;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.core.BlockIndex;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.robots.AIRobot;
import buildcraft.robots.EntityRobotBase;

public class BoardRobotLumberjack extends RedstoneBoardRobot {

	public static HashSet<BlockIndex> woodTargets = new HashSet<BlockIndex>();

	public BoardRobotLumberjack(EntityRobotBase iRobot, NBTTagCompound nbt) {
		super(iRobot);
	}

	@Override
	public void update() {
		if (robot.getItemInUse() == null) {
			startDelegateAI(new AIRobotFetchItemStack(robot, new ArrayStackFilter(new ItemStack(
					Items.wooden_axe))));
		} else {
			startDelegateAI(new AIRobotGoToWood(robot));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGoToWood) {
			BlockIndex index = ((AIRobotGoToWood) ai).woodFound;

			if (reserveWoodTarget(index)) {
				startDelegateAI(new AIRobotCutWood(robot, ((AIRobotGoToWood) ai).woodFound));
			}
		} else if (ai instanceof AIRobotCutWood) {
			synchronized (woodTargets) {
				woodTargets.remove(((AIRobotCutWood) ai).woodToChop);
			}
		}
	}

	public static boolean isFreeWoodTarget(BlockIndex index) {
		synchronized (woodTargets) {
			return !woodTargets.contains(index);
		}
	}

	public static boolean reserveWoodTarget (BlockIndex index) {
		synchronized (woodTargets) {
			if (!woodTargets.contains(index)) {
				woodTargets.add(index);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotLumberjackNBT.instance;
	}
}
