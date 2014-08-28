/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.TickHandlerCore;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.ArrayStackOrListFilter;
import buildcraft.core.inventory.filters.CompositeFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.inventory.filters.OreStackFilter;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;
import buildcraft.core.robots.AIRobotGotoBlock;
import buildcraft.core.robots.AIRobotGotoRandomGroundBlock;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotSearchBlock;
import buildcraft.core.robots.AIRobotUseToolOnBlock;
import buildcraft.core.robots.IBlockFilter;
import buildcraft.core.robots.ResourceIdBlock;
import buildcraft.silicon.statements.ActionRobotFilter;

public class BoardRobotPlanter extends RedstoneBoardRobot {

	private IStackFilter stackFilter = new CompositeFilter(new OreStackFilter("treeSapling"), new SeedFilter());
	private BlockIndex blockFound;

	public BoardRobotPlanter(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPlanterNBT.instance;
	}

	@Override
	public void update() {
		if (robot.getHeldItem() == null) {
			Collection<ItemStack> gateFilter = ActionRobotFilter.getGateFilterStacks(robot
					.getLinkedStation());

			if (gateFilter.size() != 0) {
				ArrayList<ItemStack> filteredFilter = new ArrayList<ItemStack>();

				for (ItemStack tentative : gateFilter) {
					if (stackFilter.matches(tentative)) {
						filteredFilter.add(tentative);
					}
				}

				if (filteredFilter.size() > 0) {
					ArrayStackFilter arrayFilter = new ArrayStackOrListFilter(
							filteredFilter.toArray(new ItemStack[filteredFilter.size()]));

					startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, arrayFilter));
				} else {
					startDelegateAI(new AIRobotGotoSleep(robot));
				}
			} else {
				startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, stackFilter));
			}
		} else {
			if (robot.getHeldItem().getItem() instanceof ItemSeeds) {
				startDelegateAI(new AIRobotSearchBlock(robot, new IBlockFilter() {
					@Override
					public boolean matches(World world, int x, int y, int z) {
						return BuildCraftAPI.isFarmlandProperty.get(world, x, y, z)
								&& !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))
								&& isAirAbove(world, x, y, z);
					}
				}));
			} else {
				startDelegateAI(new AIRobotGotoRandomGroundBlock(robot, 100, new IBlockFilter() {
					@Override
					public boolean matches(World world, int x, int y, int z) {
						Block b = robot.worldObj.getBlock(x, y, z);

						return b instanceof BlockDirt || b instanceof BlockGrass;
					}
				}, robot.getZoneToWork()));
			}
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoRandomGroundBlock) {
			AIRobotGotoRandomGroundBlock gotoBlock = (AIRobotGotoRandomGroundBlock) ai;

			if (gotoBlock.blockFound == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				startDelegateAI(new AIRobotUseToolOnBlock(robot, gotoBlock.blockFound));
			}
		} else if (ai instanceof AIRobotSearchBlock) {
			AIRobotSearchBlock gotoBlock = (AIRobotSearchBlock) ai;

			if (gotoBlock.blockFound != null
					&& robot.getRegistry().take(new ResourceIdBlock(gotoBlock.blockFound), robot)) {

				if (blockFound != null) {
					robot.getRegistry().release(new ResourceIdBlock(blockFound));
				}

				blockFound = gotoBlock.blockFound;
				startDelegateAI(new AIRobotGotoBlock(robot, gotoBlock.path));
			} else {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotUseToolOnBlock(robot, blockFound));
		} else if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (robot.getHeldItem() == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

	private static class SeedFilter implements IStackFilter {
		@Override
		public boolean matches(ItemStack stack) {
			return stack.getItem() instanceof ItemSeeds;
		}
	}

	private boolean isAirAbove(World world, int x, int y, int z) {
		synchronized (TickHandlerCore.startSynchronousComputation) {
			try {
				TickHandlerCore.startSynchronousComputation.wait();

				return world.isAirBlock(x, y + 1, z);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
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
}
