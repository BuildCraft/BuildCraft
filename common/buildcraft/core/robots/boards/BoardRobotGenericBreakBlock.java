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
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotBreakWithTool;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;
import buildcraft.core.robots.AIRobotSearchBlock;
import buildcraft.core.utils.IPathFound;

public abstract class BoardRobotGenericBreakBlock extends RedstoneBoardRobot {

	public BoardRobotGenericBreakBlock(EntityRobotBase iRobot) {
		super(iRobot, 1);
	}

	public abstract boolean isExpectedTool(ItemStack stack);

	public abstract boolean isExpectedBlock(World world, int x, int y, int z);

	@Override
	public final void update() {
		if (robot.getItemInUse() == null) {
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
				@Override
				public boolean matches(ItemStack stack) {
					return isExpectedTool(stack);
				}
			}));
		} else {
			startDelegateAI(new AIRobotSearchBlock(robot, new IPathFound() {
				@Override
				public boolean endReached(World world, int x, int y, int z) {
					if (isExpectedBlock(world, x, y, z)) {
						return RedstoneBoardRobot.isFreeBlock(new BlockIndex(x, y, z));
					} else {
						return false;
					}
				}
			}));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchBlock) {
			BlockIndex index = ((AIRobotSearchBlock) ai).blockFound;

			if (index != null && reserveBlock(index)) {
				startDelegateAI(new AIRobotBreakWithTool(robot, ((AIRobotSearchBlock) ai).blockFound));
			}
		} else if (ai instanceof AIRobotBreakWithTool) {
			releaseBlock(((AIRobotBreakWithTool) ai).blockToBreak);
		}
	}

}
