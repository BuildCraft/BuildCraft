/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.boards.IBoardParameter;
import buildcraft.api.boards.IBoardParameterStack;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.robots.AIRobotLookForStation;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.IStationFilter;
import buildcraft.silicon.statements.StateStationRequestItems;
import buildcraft.transport.Pipe;

public class BoardRobotPicker extends RedstoneBoardRobot {

	// TODO: Clean this when world unloaded
	public static Set<Integer> targettedItems = new HashSet<Integer>();

	private SafeTimeTracker scanTracker = new SafeTimeTracker(40, 10);

	private NBTTagCompound data;
	private RedstoneBoardNBT<?> board;
	private IBoardParameter[] params;
	private int range;
	private IStackFilter stackFilter;

	public BoardRobotPicker(EntityRobotBase robot, NBTTagCompound nbt) {
		super(robot);
		data = nbt;

		board = RedstoneBoardRegistry.instance.getRedstoneBoard(nbt);
		params = board.getParameters(nbt);

		range = nbt.getInteger("range");

		ItemStack[] stacks = new ItemStack[params.length];

		for (int i = 0; i < stacks.length; ++i) {
			IBoardParameterStack pStak = (IBoardParameterStack) params[i];
			stacks[i] = pStak.getStack();
		}

		if (stacks.length > 0) {
			stackFilter = new ArrayStackFilter(stacks);
		} else {
			stackFilter = null;
		}
	}

	@Override
	public void update() {
		if (scanTracker.markTimeIfDelay(robot.worldObj)) {
			startDelegateAI(new AIRobotFetchItem(robot, range, stackFilter));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotFetchItem) {
			if (((AIRobotFetchItem) ai).target != null) {
				// if we could get an item, let's try to get another one
				startDelegateAI(new AIRobotFetchItem(robot, range, stackFilter));
			} else {
				// otherwise, let's deliver items
				startDelegateAI(new AIRobotLookForStation(robot, new StationInventory()));
			}
		} else if (ai instanceof AIRobotLookForStation) {
			emptyContainerInInventory();
		}
	}

	private void emptyContainerInInventory() {
		DockingStation station = (DockingStation) robot.getDockingStation();

		if (station == null) {
			return;
		}

		Pipe pipe = station.pipe.pipe;

		for (int i = 0; i < robot.getSizeInventory(); ++i) {
			boolean found = false;
			ItemStack stackToAdd = robot.getStackInSlot(i);

			for (Object s : pipe.getActionStates()) {
				if (s instanceof StateStationRequestItems) {
					if (((StateStationRequestItems) s).matches(new ArrayStackFilter(stackToAdd))) {
						found = true;
						break;
					}
				}
			}

			if (!found) {
				// This stack is not accepted by any of the action states
				// currently active on this pipe - look for another one
				continue;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
						+ dir.offsetY, station.z()
						+ dir.offsetZ);

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					ITransactor trans = Transactor.getTransactorFor(nearbyTile);

					if (stackToAdd != null) {
						ItemStack added = trans.add(stackToAdd, dir, true);
						robot.decrStackSize(i, added.stackSize);
					}
				}
			}
		}
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPickerNBT.instance;
	}

	private class StationInventory implements IStationFilter {
		@Override
		public boolean matches(DockingStation station) {
			Pipe pipe = station.pipe.pipe;

			for (int i = 0; i < robot.getSizeInventory(); ++i) {
				boolean found = false;
				ItemStack stackToAdd = robot.getStackInSlot(i);

				for (Object s : pipe.getActionStates()) {
					if (s instanceof StateStationRequestItems) {
						if (((StateStationRequestItems) s).matches(new ArrayStackFilter(stackToAdd))) {
							found = true;
							break;
						}
					}
				}

				if (!found) {
					// This stack is not accepted by any of the action states
					// currently active on this pipe - look for another one
					continue;
				}

				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
							+ dir.offsetY, station.z()
							+ dir.offsetZ);

					if (nearbyTile != null && nearbyTile instanceof IInventory) {
						ITransactor trans = Transactor.getTransactorFor(nearbyTile);

						if (stackToAdd != null && trans.add(stackToAdd, dir, false) != null) {
							// We can add the item to this inventory, go to this
							// station.

							return true;
						}

					}
				}

			}

			return false;
		}
	}
}
