/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import net.minecraft.item.ItemStack;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.ai.AIRobotBreak;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoSleep;

public abstract class BoardRobotGenericBreakBlock extends BoardRobotGenericSearchBlock {

	public BoardRobotGenericBreakBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public abstract boolean isExpectedTool(ItemStack stack);

	@Override
	public final void update() {
		if (!isExpectedTool(null) && robot.getHeldItem() == null) {
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
				@Override
				public boolean matches(ItemStack stack) {
					return isExpectedTool(stack);
				}
			}));
		} else if (blockFound() != null) {
			startDelegateAI(new AIRobotBreak(robot, blockFound()));
		} else {
			super.update();
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotBreak) {
			releaseBlockFound(ai.success());
		}
		super.delegateAIEnded(ai);
	}
}
