/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.boards;

import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.utils.IBlockFilter;
import buildcraft.robots.ResourceIdBlock;
import buildcraft.robots.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robots.ai.AIRobotGotoBlock;
import buildcraft.robots.ai.AIRobotGotoSleep;
import buildcraft.robots.ai.AIRobotSearchBlock;
import buildcraft.robots.ai.AIRobotUseToolOnBlock;
import buildcraft.robots.RobotRegistry;

public class BoardRobotFarmer extends RedstoneBoardRobot {

	private BlockIndex blockFound;

	public BoardRobotFarmer(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotFarmerNBT.instance;
	}

	@Override
	public void update() {
		if (robot.getHeldItem() == null) {
			startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, new IStackFilter() {
				@Override
				public boolean matches(ItemStack stack) {
					return stack != null && stack.getItem() instanceof ItemHoe;
				}
			}));
		} else {
			startDelegateAI(new AIRobotSearchBlock(robot, new IBlockFilter() {
				@Override
				public boolean matches(World world, int x, int y, int z) {
					return BuildCraftAPI.isDirtProperty.get(world, x, y, z)
							&& !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))
							&& isAirAbove(world, x, y, z);
				}
			}));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchBlock) {
			AIRobotSearchBlock searchAI = (AIRobotSearchBlock) ai;

			if (searchAI.blockFound != null
					&& RobotRegistry.getRegistry(robot.worldObj).take(
							new ResourceIdBlock(searchAI.blockFound), robot)) {
				((AIRobotSearchBlock) ai).unreserve();

				if (blockFound != null) {
					robot.getRegistry().release(new ResourceIdBlock(blockFound));
				}

				blockFound = searchAI.blockFound;
				startDelegateAI(new AIRobotGotoBlock(robot, searchAI.path));
			} else {
				if (searchAI.blockFound != null) {
					((AIRobotSearchBlock) ai).unreserve();
				}
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotUseToolOnBlock(robot, blockFound));
		} else if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (robot.getHeldItem() == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotUseToolOnBlock) {
			robot.getRegistry().release(new ResourceIdBlock(blockFound));
			blockFound = null;
		}
	}

	@Override
	public void end() {
		if (blockFound != null) {
			robot.getRegistry().release(new ResourceIdBlock(blockFound));
		}
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockFound != null) {
			NBTTagCompound sub = new NBTTagCompound();
			blockFound.writeTo(sub);
			nbt.setTag("blockFound", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockFound")) {
			blockFound = new BlockIndex(nbt.getCompoundTag("blockFound"));
		}
	}

	private boolean isAirAbove(World world, int x, int y, int z) {
		synchronized (world) {
			return world.isAirBlock(x, y + 1, z);
		}
	}
}
