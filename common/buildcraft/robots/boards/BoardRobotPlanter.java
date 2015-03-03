/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.boards;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.ArrayStackOrListFilter;
import buildcraft.core.inventory.filters.CompositeFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.utils.IBlockFilter;
import buildcraft.robots.ResourceIdBlock;
import buildcraft.robots.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robots.ai.AIRobotGotoBlock;
import buildcraft.robots.ai.AIRobotGotoSleep;
import buildcraft.robots.ai.AIRobotSearchBlock;
import buildcraft.robots.ai.AIRobotSearchBlockBase;
import buildcraft.robots.ai.AIRobotSearchRandomBlock;
import buildcraft.robots.ai.AIRobotUseToolOnBlock;
import buildcraft.robots.statements.ActionRobotFilter;

public class BoardRobotPlanter extends RedstoneBoardRobot {

	private IStackFilter stackFilter = new CompositeFilter(new PlantableFilter(), new ReedFilter());
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
			final ItemStack itemStack = robot.getHeldItem();
			IBlockFilter blockFilter;
			if (itemStack.getItem() instanceof ItemReed) {
				blockFilter = new IBlockFilter() {
					@Override
					public boolean matches(World world, int x, int y, int z) {
						return world.getBlock(x, y, z).canSustainPlant(world, x, y, z, ForgeDirection.UP, (IPlantable) Blocks.reeds)
								&& world.getBlock(x, y, z) != Blocks.reeds
								&& !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))
								&& isAirAbove(world, x, y, z);
					}
				};
			} else if (itemStack.getItem() instanceof ItemBlock) {
				final Block plantBlock = ((ItemBlock) itemStack.getItem()).field_150939_a;
				blockFilter = new IBlockFilter() {
					@Override
					public boolean matches(World world, int x, int y, int z) {
						return world.getBlock(x, y, z).canSustainPlant(world, x, y, z, ForgeDirection.UP, (IPlantable) plantBlock)
								&& world.getBlock(x, y, z) != plantBlock
								&& !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))
								&& isAirAbove(world, x, y, z);
					}
				};
			} else {
				blockFilter = new IBlockFilter() {
					@Override
					public boolean matches(World world, int x, int y, int z) {
						return world.getBlock(x, y, z).canSustainPlant(world, x, y, z, ForgeDirection.UP, (IPlantable) itemStack.getItem())
								&& !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))
								&& isAirAbove(world, x, y, z);
					}
				};
			}
			startDelegateAI(new AIRobotSearchRandomBlock(robot, blockFilter));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchRandomBlock) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				releaseBlockFound();
				AIRobotSearchRandomBlock searchAI = (AIRobotSearchRandomBlock) ai;
				if (searchAI.takeResource()) {
					blockFound = searchAI.blockFound;
					searchAI.path.removeLast();
					startDelegateAI(new AIRobotGotoBlock(robot, searchAI.path));
				} else {
					startDelegateAI(new AIRobotGotoSleep(robot));
				}
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotUseToolOnBlock(robot, blockFound));
		} else if (ai instanceof AIRobotUseToolOnBlock) {
			releaseBlockFound();
		} else if (ai instanceof AIRobotFetchAndEquipItemStack) {
			if (robot.getHeldItem() == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

	private void releaseBlockFound() {
		if (blockFound != null) {
			robot.getRegistry().release(new ResourceIdBlock(blockFound));
			blockFound = null;
		}
	}

	private static class PlantableFilter implements IStackFilter {
		@Override
		public boolean matches(ItemStack stack) {
			if (stack.getItem() instanceof IPlantable) {
				return true;
			}
			if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).field_150939_a instanceof IPlantable) {
				return true;
			}
			return false;
		}
	}

	private static class ReedFilter implements IStackFilter {
		@Override
		public boolean matches(ItemStack stack) {
			return stack.getItem() instanceof ItemReed;
		}
	}

	private boolean isAirAbove(World world, int x, int y, int z) {
		synchronized (world) {
			return world.isAirBlock(x, y + 1, z);
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
