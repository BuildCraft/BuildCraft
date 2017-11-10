/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.core.lib.utils.IEntityFilter;
import buildcraft.robotics.ai.AIRobotAttack;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationAndUnload;
import buildcraft.robotics.ai.AIRobotSearchEntity;

public class BoardRobotKnight extends RedstoneBoardRobot {

	public BoardRobotKnight(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BCBoardNBT.REGISTRY.get("knight");
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
		} else if (robot.getHeldItem() != null && robot.getHeldItem().getItemDamage() >= robot.getHeldItem().getMaxDamage()) {
			startDelegateAI(new AIRobotGotoStationAndUnload(robot));
		} else {
			startDelegateAI(new AIRobotSearchEntity(robot, new IEntityFilter() {
				@Override
				public boolean matches(Entity entity) {
					return (entity instanceof IMob) || (entity instanceof EntityWolf && ((EntityWolf) entity).isAngry());
				}
			}, 250, robot.getZoneToWork()));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotSearchEntity) {
			if (ai.success()) {
				startDelegateAI(new AIRobotAttack(robot, ((AIRobotSearchEntity) ai).target));
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}
}
