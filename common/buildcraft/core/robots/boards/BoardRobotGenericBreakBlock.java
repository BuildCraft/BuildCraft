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
import buildcraft.core.robots.AIRobotBreak;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotSearchBlock;
import buildcraft.core.utils.IPathFound;

public abstract class BoardRobotGenericBreakBlock extends RedstoneBoardRobot {

	private BlockIndex indexStored;

	public BoardRobotGenericBreakBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public abstract boolean isExpectedTool(ItemStack stack);

	public abstract boolean isExpectedBlock(World world, int x, int y, int z);

	public final void preemt(AIRobot ai) {
		if (ai instanceof AIRobotSearchBlock) {
			BlockIndex index = ((AIRobotSearchBlock) ai).blockFound;

			if (!RedstoneBoardRobot.isFreeBlock(index)) {
				abortDelegateAI();
			}
		}
	}

	@Override
	public final void update() {
		if (!isExpectedTool(null) && robot.getHeldItem() == null) {
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
			indexStored = ((AIRobotSearchBlock) ai).blockFound;

			if (indexStored == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				if (reserveBlock(indexStored)) {
					startDelegateAI(new AIRobotBreak(robot, indexStored));
				}
			}
		} else if (ai instanceof AIRobotBreak) {
			releaseBlock(indexStored);
			indexStored = null;
		}
	}

	@Override
	public void end() {
		if (indexStored != null) {
			releaseBlock(indexStored);
		}
	}

}
