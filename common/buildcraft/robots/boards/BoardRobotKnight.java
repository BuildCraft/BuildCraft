/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.boards;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.utils.IEntityFilter;
import buildcraft.robots.ai.AIRobotAttack;
import buildcraft.robots.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robots.ai.AIRobotGotoSleep;
import buildcraft.robots.ai.AIRobotSearchEntity;

public class BoardRobotKnight extends RedstoneBoardRobot {

	public BoardRobotKnight(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotKnightNBT.instance;
	}

	@Override
	public final void update() {
		if (robot.getHeldItem() == null) {
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
				@Override
				public boolean matches(ItemStack stack) {
					return stack.getItem() instanceof ItemSword;
				}
			}));
		} else {
			startDelegateAI(new AIRobotSearchEntity(robot, new IEntityFilter() {
				@Override
				public boolean matches(Entity entity) {
					return entity instanceof EntityMob;
				}
			}, 250, robot.getZoneToWork()));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (robot.getHeldItem() == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotSearchEntity) {
			AIRobotSearchEntity mobAI = (AIRobotSearchEntity) ai;

			if (mobAI.target != null) {
				startDelegateAI(new AIRobotAttack(robot, ((AIRobotSearchEntity) ai).target));
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}
}
