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

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.TickHandlerCore;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotBreak;
import buildcraft.core.robots.AIRobotFetchAndEquipItemStack;
import buildcraft.core.robots.AIRobotGotoBlock;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotSearchBlock;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.IBlockFilter;
import buildcraft.core.robots.ResourceIdBlock;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

public abstract class BoardRobotGenericBreakBlock extends RedstoneBoardRobot {

	private BlockIndex indexStored;
	private ArrayList<Block> blockFilter = new ArrayList<Block>();
	private ArrayList<Integer> metaFilter = new ArrayList<Integer>();

	public BoardRobotGenericBreakBlock(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public abstract boolean isExpectedTool(ItemStack stack);

	/**
	 * This function has to be derived in a thread safe manner, as it may be
	 * called from parallel jobs. In particular, world should not be directly
	 * used, only through WorldProperty class and subclasses.
	 */
	public abstract boolean isExpectedBlock(World world, int x, int y, int z);

	@Override
	public final void start() {
		DockingStation station = (DockingStation) robot.getLinkedStation();

		for (ActionSlot slot : new ActionIterator(station.getPipe().pipe)) {
			if (slot.action instanceof ActionRobotFilter) {
				for (IActionParameter p : slot.parameters) {
					if (p != null && p instanceof ActionParameterItemStack) {
						ActionParameterItemStack param = (ActionParameterItemStack) p;
						ItemStack stack = param.getItemStackToDraw();

						if (stack != null && stack.getItem() instanceof ItemBlock) {
							blockFilter.add(((ItemBlock) stack.getItem()).field_150939_a);
							metaFilter.add(stack.getItemDamage());
						}
					}
				}
			}
		}
	}

	public final void preemt(AIRobot ai) {
		if (ai instanceof AIRobotSearchBlock) {
			BlockIndex index = ((AIRobotSearchBlock) ai).blockFound;

			if (!robot.getRegistry().isTaken(new ResourceIdBlock(index))) {
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
			startDelegateAI(new AIRobotSearchBlock(robot, new IBlockFilter() {
				@Override
				public boolean matches(World world, int x, int y, int z) {
					if (isExpectedBlock(world, x, y, z) && matchesGateFilter(world, x, y, z)) {
						return robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z));
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
			if (indexStored != null) {
				robot.getRegistry().release(new ResourceIdBlock(indexStored));
			}

			indexStored = ((AIRobotSearchBlock) ai).blockFound;

			if (indexStored == null) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				if (robot.getRegistry().take(new ResourceIdBlock(indexStored), robot)) {
					startDelegateAI(new AIRobotGotoBlock(robot, ((AIRobotSearchBlock) ai).path));
				}
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotBreak(robot, indexStored));
		} else if (ai instanceof AIRobotBreak) {
			robot.getRegistry().release(new ResourceIdBlock(indexStored));
			indexStored = null;
		}
	}

	@Override
	public void end() {
		if (indexStored != null) {
			robot.getRegistry().release(new ResourceIdBlock(indexStored));
		}
	}

	private boolean matchesGateFilter(World world, int x, int y, int z) {
		if (blockFilter.size() == 0) {
			return true;
		}

		synchronized (TickHandlerCore.startSynchronousComputation) {
			try {
				TickHandlerCore.startSynchronousComputation.wait();

				Block block = world.getBlock(x, y, z);
				int meta = world.getBlockMetadata(x, y, z);

				for (int i = 0; i < blockFilter.size(); ++i) {
					if (blockFilter.get(i) == block && metaFilter.get(i) == meta) {
						return true;
					}
				}

				return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (indexStored != null) {
			NBTTagCompound sub = new NBTTagCompound();
			indexStored.writeTo(sub);
			nbt.setTag("indexStored", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("indexStored")) {
			indexStored = new BlockIndex (nbt.getCompoundTag("indexStored"));
		}
	}
}
