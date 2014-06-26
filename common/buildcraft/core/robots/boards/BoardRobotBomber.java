/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotGotoStationToLoad;
import buildcraft.core.robots.AIRobotLoad;

public class BoardRobotBomber extends RedstoneBoardRobot {

	private static final IStackFilter TNT_FILTER = new ArrayStackFilter(new ItemStack(Blocks.tnt));

	public BoardRobotBomber(EntityRobotBase iRobot) {
		super(iRobot, 0);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotBomberNBT.instance;
	}

	@Override
	public final void update() {
		boolean containItems = false;

		for (int i = 0; i < robot.getSizeInventory(); ++i) {
			if (robot.getStackInSlot(i) != null) {
				containItems = true;
			}
		}

		if (!containItems) {
			startDelegateAI(new AIRobotGotoStationToLoad(robot, TNT_FILTER));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			startDelegateAI(new AIRobotLoad(robot, TNT_FILTER));
		}
	}
}
