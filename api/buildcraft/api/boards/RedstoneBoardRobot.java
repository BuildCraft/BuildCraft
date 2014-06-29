/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.boards;

import java.util.HashSet;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;

public abstract class RedstoneBoardRobot extends AIRobot implements IRedstoneBoard<EntityRobotBase> {

	public static HashSet<BlockIndex> reservedBlocks = new HashSet<BlockIndex>();

	public RedstoneBoardRobot(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public abstract RedstoneBoardRobotNBT getNBTHandler();

	@Override
	public final void updateBoard(EntityRobotBase container) {

	}

	public static boolean isFreeBlock(BlockIndex index) {
		synchronized (reservedBlocks) {
			return !reservedBlocks.contains(index);
		}
	}

	public static boolean reserveBlock(BlockIndex index) {
		synchronized (reservedBlocks) {
			if (!reservedBlocks.contains(index)) {
				reservedBlocks.add(index);
				return true;
			} else {
				return false;
			}
		}
	}

	public static void releaseBlock(BlockIndex index) {
		synchronized (reservedBlocks) {
			if (reservedBlocks.contains(index)) {
				reservedBlocks.remove(index);
			}
		}
	}

}
