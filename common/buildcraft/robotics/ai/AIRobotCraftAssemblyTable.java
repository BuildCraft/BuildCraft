/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.api.robots.RobotManager;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.inventory.InventoryCopy;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.ArrayStackFilter;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationAllowCraft;
import buildcraft.silicon.BlockLaserTable;
import buildcraft.silicon.ResourceIdAssemblyTable;
import buildcraft.silicon.TileAssemblyTable;

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
		this(iRobot);

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
					startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ReqStackFilter(), 1));
					return;
				}

				if (robot.getDockingStation() != stationFound) {
					startDelegateAI(new AIRobotGotoStation(robot, stationFound));

					return;
				}

				ITransactor trans = Transactor.getTransactorFor(table);

				for (IInvSlot s : InventoryIterator.getIterable(robot)) {
					if (s.getStackInSlot() != null) {
						ItemStack added = trans.add(s.getStackInSlot(), ForgeDirection.UNKNOWN, true);

						if (added.stackSize == 0) {
							terminate();
						} else if (added.stackSize == s.getStackInSlot().stackSize) {
							s.setStackInSlot(null);
						} else {
							s.getStackInSlot().stackSize -= added.stackSize;
						}
					}
				}

				RobotManager.registryProvider.getRegistry(robot.worldObj).take(new ResourceIdAssemblyTable(table), robot);
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
				table = getUsableAssemblyTable(new BlockIndex(stationFound.x(), stationFound.y(), stationFound.z()));

				if (table == null) {
					terminate();
					return;
				}

				BlockIndex index = new BlockIndex(table);

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
			ItemStack added = robotTransactor.add(stack, ForgeDirection.UNKNOWN, true);

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

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if (getUsableAssemblyTable(new BlockIndex(station.x(), station.y(), station.z())) != null) {
					return true;
				}
			}

			return false;
		}
	}

	private TileAssemblyTable getUsableAssemblyTable(BlockIndex b) {

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			BlockIndex index = new BlockIndex (b.x + dir.offsetX, b.y
					+ dir.offsetY, b.z
					+ dir.offsetZ);

			if (robot.getRegistry().isTaken(new ResourceIdBlock(index))) {
				continue;
			}

			Block nearbyBlock = robot.worldObj.getBlock(index.x, index.y, index.z);
			int nearbyMeta = robot.worldObj.getBlockMetadata(index.x, index.y, index.z);

			if (nearbyBlock instanceof BlockLaserTable && nearbyMeta == 0) {
				TileAssemblyTable f = (TileAssemblyTable) robot.worldObj.getTileEntity(index.x, index.y, index.z);

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
