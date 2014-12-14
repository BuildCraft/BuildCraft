/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import net.minecraft.util.BlockPos;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotGotoBlock;
import buildcraft.core.robots.AIRobotGotoStationToLoad;
import buildcraft.core.robots.AIRobotLoad;
import buildcraft.core.robots.AIRobotSearchRandomGroundBlock;
import buildcraft.core.robots.IBlockFilter;

public class BoardRobotBomber extends RedstoneBoardRobot {

	private static final IStackFilter TNT_FILTER = new ArrayStackFilter(new ItemStack(Blocks.tnt));

	private BlockPos target = null;

	private int flyingHeight = 20;

	public BoardRobotBomber(EntityRobotBase iRobot) {
		super(iRobot);
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
			startDelegateAI(new AIRobotGotoStationToLoad(robot, TNT_FILTER, null));
		} else {
			startDelegateAI(new AIRobotSearchRandomGroundBlock(robot, 100, new IBlockFilter() {
				@Override
				public boolean matches(World world, int x, int y, int z) {
					return y < world.getActualHeight() - flyingHeight && !world.isAirBlock(x, y, z);
				}
			}, robot.getZoneToWork()));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			startDelegateAI(new AIRobotLoad(robot, TNT_FILTER));
		} else if (ai instanceof AIRobotSearchRandomGroundBlock) {
			AIRobotSearchRandomGroundBlock aiFind = (AIRobotSearchRandomGroundBlock) ai;

			startDelegateAI(new AIRobotGotoBlock(robot, aiFind.blockFound.x, aiFind.blockFound.y + flyingHeight,
					aiFind.blockFound.z));
		} else if (ai instanceof AIRobotGotoBlock) {
			ITransactor t = Transactor.getTransactorFor(robot);
			ItemStack stack = t.remove(TNT_FILTER, null, true);

			if (stack != null && stack.stackSize > 0) {
				EntityTNTPrimed tnt = new EntityTNTPrimed(robot.worldObj, robot.posX + 0.25, robot.posY - 1,
					robot.posZ + 0.25,
					robot);
				tnt.fuse = 37;
				robot.worldObj.spawnEntityInWorld(tnt);
				robot.worldObj.playSoundAtEntity(tnt, "game.tnt.primed", 1.0F, 1.0F);
			}
		}
	}
}
