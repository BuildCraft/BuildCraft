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
import net.minecraft.block.BlockFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;

public class AIRobotCraftFurnace extends AIRobotCraftGeneric {

	private static final int INPUT_SLOT = 0;
	private static final int FUEL_SLOT = 1;
	private static final int OUTPUT_SLOT = 2;

	private ItemStack input;
	private DockingStation stationFound;
	private TileEntityFurnace furnace;

	public AIRobotCraftFurnace(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotCraftFurnace(EntityRobotBase iRobot, ItemStack iInput) {
		super(iRobot);

		input = iInput;
	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotSearchStation(robot, new StationFurnaceFilter(), robot.getZoneToWork()));
	}

	@Override
	public void update() {
		if (furnace != null) {
			if (furnace.getStackInSlot(FUEL_SLOT) == null && getItem(new FuelFilter()) == null) {
				startDelegateAI(new AIRobotGotoStationAndLoad(robot, new FuelFilter(), robot.getZoneToWork()));

				return;
			}

			if (getItem(new ArrayStackFilter(input)) == null) {
				startDelegateAI(new AIRobotGotoStationAndLoad(robot, new ArrayStackFilter(input), robot.getZoneToWork()));

				return;
			}

			if (robot.getDockingStation() != stationFound) {
				startDelegateAI(new AIRobotGotoStation(robot, stationFound));

				return;
			}

			// TODO: Does this need a timing thing?

			if (furnace.getStackInSlot(FUEL_SLOT) == null) {
				IInvSlot s = getItem(new FuelFilter());
				furnace.setInventorySlotContents(FUEL_SLOT, s.decreaseStackInSlot(1));
			}

			if (furnace.getStackInSlot(INPUT_SLOT) == null) {
				IInvSlot s = getItem(new ArrayStackFilter(input));
				furnace.setInventorySlotContents(INPUT_SLOT, s.decreaseStackInSlot(1));
			}

			// TODO: Create a delegate AI, waiting until the thing is done

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
				furnace = getUsableFurnace(new BlockIndex(stationFound.x(), stationFound.y(), stationFound.z()));

				if (furnace == null) {
					terminate();
					return;
				}

				BlockIndex index = new BlockIndex(furnace);

				if (!reserveBlock(index)) {
					terminate();
				}

				robot.reserveStation(stationFound);
			}
		} else if (ai instanceof AIRobotGotoStationAndLoad) {

		}
	}

	@Override
	protected ArrayList<ArrayStackFilter> tryCraft(boolean doRemove) {
		// TODO Auto-generated method stub
		return null;
	}


	// How to operate furnaces
	// [1] identify a furnace
	// [2] verify that proper item is in. If empty, and slot out empty or
	// contains order get proper item, otherwise skip
	// [3] bring proper item and put in
	// [4] as soon as output contains expected item, get it and place it
	// somewhere

	// How to operate assembly tables

	private class StationFurnaceFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if (getUsableFurnace(new BlockIndex(station.x(), station.y(), station.z())) != null) {
					return true;
				}
			}

			return false;
		}
	}

	private class FuelFilter implements IStackFilter {

		@Override
		public boolean matches(ItemStack stack) {
			return TileEntityFurnace.getItemBurnTime(stack) > 0 && !StackHelper.isMatchingItem(stack, input);
		}
	}

	private TileEntityFurnace getUsableFurnace(BlockIndex b) {
		// reserve that furnace if found from the block reserve system

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			BlockIndex index = new BlockIndex (b.x + dir.offsetX, b.y
					+ dir.offsetY, b.z
					+ dir.offsetZ);

			if (!isFreeBlock(index)) {
				continue;
			}

			Block nearbyBlock = robot.worldObj.getBlock(index.x, index.y, index.z);

			if (nearbyBlock instanceof BlockFurnace) {
				TileEntityFurnace f = (TileEntityFurnace) robot.worldObj.getTileEntity(index.x, index.y, index.z);

				if (f.getStackInSlot(INPUT_SLOT) != null
						&& !StackHelper.isMatchingItem(input, f.getStackInSlot(INPUT_SLOT))) {

					continue;
				}

				return f;
			}
		}

		return null;
	}

	private IInvSlot getItem(IStackFilter filter) {
		for (IInvSlot s : InventoryIterator.getIterable(robot)) {
			if (s.getStackInSlot() != null && filter.matches(s.getStackInSlot())) {
				return s;
			}
		}

		return null;
	}

}
