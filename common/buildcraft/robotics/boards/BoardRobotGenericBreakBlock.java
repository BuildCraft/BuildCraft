/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.robotics.DockingStation;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.robotics.ai.AIRobotBreak;
import buildcraft.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraft.robotics.ai.AIRobotGotoBlock;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotSearchBlock;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.StatementSlot;

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
			updateFilter();

			startDelegateAI(new AIRobotSearchBlock(robot, new IBlockFilter() {
				@Override
				public boolean matches(World world, int x, int y, int z) {
					if (isExpectedBlock(world, x, y, z) && !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))) {
						return matchesGateFilter(world, x, y, z);
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
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				releaseBlockFound();
				AIRobotSearchBlock searchAI = (AIRobotSearchBlock) ai;
				if (searchAI.takeResource()) {
					indexStored = searchAI.blockFound;
					startDelegateAI(new AIRobotGotoBlock(robot, searchAI.path));
				} else {
					startDelegateAI(new AIRobotGotoSleep(robot));
				}
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			startDelegateAI(new AIRobotBreak(robot, indexStored));
		} else if (ai instanceof AIRobotBreak) {
			releaseBlockFound();
			startDelegateAI(new AIRobotGotoSleep(robot));
		}
	}

	private void releaseBlockFound() {
		if (indexStored != null) {
			robot.getRegistry().release(new ResourceIdBlock(indexStored));
			indexStored = null;
		}
	}

	@Override
	public void end() {
		releaseBlockFound();
	}

	public final void updateFilter() {
		blockFilter.clear();
		metaFilter.clear();

		DockingStation station = (DockingStation) robot.getLinkedStation();

		for (StatementSlot slot : new ActionIterator(station.getPipe().pipe)) {
			if (slot.statement instanceof ActionRobotFilter) {
				for (IStatementParameter p : slot.parameters) {
					if (p != null && p instanceof StatementParameterItemStack) {
						StatementParameterItemStack param = (StatementParameterItemStack) p;
						ItemStack stack = param.getItemStack();

						if (stack != null && stack.getItem() instanceof ItemBlock) {
							blockFilter.add(((ItemBlock) stack.getItem()).field_150939_a);
							metaFilter.add(stack.getItemDamage());
						}
					}
				}
			}
		}
	}

	private boolean matchesGateFilter(World world, int x, int y, int z) {
		if (blockFilter.size() == 0) {
			return true;
		}

        Block block;
        int meta;
		synchronized (world) {
            block = world.getBlock(x, y, z);
            meta = world.getBlockMetadata(x, y, z);
		}

        for (int i = 0; i < blockFilter.size(); ++i) {
            if (blockFilter.get(i) == block && metaFilter.get(i) == meta) {
                return true;
            }
        }

        return false;
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
