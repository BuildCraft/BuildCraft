/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryCopy;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.silicon.BlockLaserTable;
import buildcraft.silicon.TileAssemblyTable;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.silicon.statements.ActionStationAllowCraft;

public class AIRobotCraftAssemblyTable extends AIRobotCraftGeneric {

	private CraftingResult<ItemStack> expectedResult;
	private DockingStation stationFound;
	private TileAssemblyTable table;
	private boolean craftStarted = false;
	private ArrayList<ArrayStackFilter> requirements;

	private int waitedTime = 0;

	public AIRobotCraftAssemblyTable(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotCraftAssemblyTable(EntityRobotBase iRobot, CraftingResult craftingResult) {
		super(iRobot);

		expectedResult = craftingResult;
	}

	@Override
	public void start() {
		requirements = tryCraft();
		startDelegateAI(new AIRobotSearchStation(robot, new StationAssemblyTableFilter(), robot.getZoneToWork()));
	}

	@Override
	public void update() {
		if (table != null) {
			if (!craftStarted) {
				if (requirements.size() != 0) {
					startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ReqStackFilter(), robot.getZoneToWork()));
					return;
				}

				if (robot.getDockingStation() != stationFound) {
					startDelegateAI(new AIRobotGotoStation(robot, stationFound));

					return;
				}

				ITransactor trans = Transactor.getTransactorFor(table);

				for (IInvSlot s : InventoryIterator.getIterable(robot)) {
					if (s.getStackInSlot() != null) {
						ItemStack added = trans.add(s.getStackInSlot(), null, true);

						if (added.stackSize == 0) {
							terminate();
						} else if (added.stackSize == s.getStackInSlot().stackSize) {
							s.setStackInSlot(null);
						} else {
							s.getStackInSlot().stackSize -= added.stackSize;
						}
					}
				}

				RobotRegistry.getRegistry(robot.worldObj).take(new ResourceIdAssemblyTable(table), robot);
				table.planOutput(expectedResult.recipe);
				// TODO: How to make sure this output is not crafted more than
				// once??

				craftStarted = true;
			} else {
				waitedTime++;

				if (InvUtils.getItem(robot, new ArrayStackFilter(expectedResult.crafted)) != null) {
					crafted = true;
					terminate();
				} else if (waitedTime > 120 * 60) {
					terminate();
				}
			}
		} else {
			terminate();
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchStation) {
			if (!ai.success()) {
				crafted = false;
				terminate();
			} else {
				stationFound = ((AIRobotSearchStation) ai).targetStation;
				table = getUsableAssemblyTable(stationFound.pos());

				if (table == null) {
					terminate();
					return;
				}

				BlockPos index = table.getPos();

				if (!robot.getRegistry().take(new ResourceIdBlock(index), robot)) {
					terminate();
				}

				if (!stationFound.take(robot)) {
					terminate();
				}
			}
		} else if (ai instanceof AIRobotGotoStationAndLoad) {
			if (!ai.success()) {
				terminate();
			} else {
				requirements = tryCraft();
			}
		}
	}

	@Override
	public ItemStack receiveItem(ItemStack stack) {
		if (StackHelper.isMatchingItem(stack, expectedResult.crafted)) {
			ITransactor robotTransactor = Transactor.getTransactorFor(robot);
			ItemStack added = robotTransactor.add(stack, null, true);

			stack.stackSize -= added.stackSize;

			return stack;
		} else {
			return stack;
		}
	}

	protected ArrayList<ArrayStackFilter> tryCraft() {
		Object[] items = expectedResult.usedItems.toArray();

		ArrayList<ArrayStackFilter> result = new ArrayList<ArrayStackFilter>();

		IInventory inv = new InventoryCopy(robot);

		for (Object tmp : items) {
			if (tmp == null) {
				continue;
			}

			int qty = 0;
			ArrayStackFilter filter;

			if (tmp instanceof ItemStack) {
				ItemStack stack = (ItemStack) tmp;
				qty = stack.stackSize;
				filter = new ArrayStackFilter(stack);
			} else {
				ArrayList<ItemStack> stacks = (ArrayList<ItemStack>) tmp;
				qty = stacks.get(0).stackSize;
				filter = new ArrayStackFilter(stacks.toArray(new ItemStack[stacks.size()]));
			}

			for (IInvSlot s : InventoryIterator.getIterable(inv)) {
				if (filter.matches(s.getStackInSlot())) {
					ItemStack removed = s.decreaseStackInSlot(qty);

					qty = qty - removed.stackSize;

					if (removed.stackSize == 0) {
						break;
					}
				}
			}

			if (qty > 0) {
				result.add(filter);
			}
		}

		return result;
	}

	private class StationAssemblyTableFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			if (!ActionRobotFilter.canInteractWithItem(station, new ArrayStackFilter(expectedResult.crafted),
					ActionStationAllowCraft.class)) {
				return false;
			}

			for (EnumFacing dir : EnumFacing.values()) {
				if (getUsableAssemblyTable(station.pos()) != null) {
					return true;
				}
			}

			return false;
		}
	}

	private TileAssemblyTable getUsableAssemblyTable(BlockPos b) {

		for (EnumFacing dir : EnumFacing.values()) {
			BlockPos index = b.offset(dir);

			if (robot.getRegistry().isTaken(new ResourceIdBlock(index))) {
				continue;
			}

			IBlockState nearbyState = robot.worldObj.getBlockState(index);
			Block nearbyBlock = nearbyState.getBlock();

			if (nearbyBlock instanceof BlockLaserTable) {
				TileAssemblyTable f = (TileAssemblyTable) robot.worldObj.getTileEntity(index);

				// TODO: check if assembly table has some empty slots

				return f;
			}
		}

		return null;
	}

	private class ReqStackFilter implements IStackFilter {
		@Override
		public boolean matches(ItemStack stack) {
			for (ArrayStackFilter s : requirements) {
				if (s.matches(stack)) {
					return true;
				}
			}

			return false;
		}
	}
}
