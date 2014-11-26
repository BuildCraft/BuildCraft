/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.IStackFilter;

public class AIRobotFetchAndEquipItemStack extends AIRobot {

	private IStackFilter filter;

	public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot, IStackFilter iFilter) {
		super(iRobot);

		filter = iFilter;
	}

	@Override
	public void update() {
		startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, null));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (filter == null) {
			// filter can't be retreived, usually because of a load operation.
			// Force a hard abort, preventing parent AI to continue normal
			// sequence of actions and possibly re-starting this.
			abort();
			return;
		}

		if (robot.getDockingStation() != null) {
			DockingStation station = (DockingStation) robot.getDockingStation();

			ItemStack itemFound = null;

			for (EnumFacing dir : EnumFacing.values()) {
				TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.getFrontOffsetX(),
						station.y()
								+ dir.getFrontOffsetY(), station.z() + dir.getFrontOffsetZ());

				if (nearbyTile != null && nearbyTile instanceof IInventory) {
					ITransactor trans = Transactor.getTransactorFor(nearbyTile);

					itemFound = trans.remove(filter, dir.getOpposite(), true);

					if (itemFound != null) {
						break;
					}
				}
			}

			if (itemFound != null) {
				robot.setItemInUse(itemFound);
				terminate();
			}
		}

		terminate();
	}
}
