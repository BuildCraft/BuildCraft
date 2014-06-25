/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotAttack;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;
import buildcraft.core.robots.AIRobotGotoMob;

public class BoardRobotKnight extends RedstoneBoardRobot {

	public BoardRobotKnight(EntityRobotBase iRobot) {
		super(iRobot, 0);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotKnightNBT.instance;
	}

	@Override
	public void preempt(AIRobot ai) {
		if (ai instanceof AIRobotGotoMob) {
			AIRobotGotoMob mobAI = (AIRobotGotoMob) ai;

			if (robot.getDistanceToEntity(mobAI.target) < 2.0) {
				startDelegateAI(new AIRobotAttack(robot, mobAI.target));
			}
		}
	}

	@Override
	public final void update() {
		if (robot.getItemInUse() == null) {
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
				@Override
				public boolean matches(ItemStack stack) {
					return stack.getItem() instanceof ItemSword;
				}
			}));
		} else {
			startDelegateAI(new AIRobotGotoMob(robot, 250));
		}
	}

}
