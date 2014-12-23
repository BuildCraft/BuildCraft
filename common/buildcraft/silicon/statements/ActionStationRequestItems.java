/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.gates.StatementSlot;

public class ActionStationRequestItems extends ActionStationInputItems {

	public ActionStationRequestItems() {
		super("buildcraft:station.request_items");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.request_items");
	}

	/*@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_request_items");
	}*/

	@Override
	public int maxParameters() {
		return 3;
	}

	@Override
	public int minParameters() {
		return 1;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}

	@Override
	public boolean insert(DockingStation station, EntityRobot robot, StatementSlot actionSlot, IInvSlot invSlot,
			boolean doInsert) {
		if (!super.insert(station, robot, actionSlot, invSlot, doInsert)) {
			return false;
		}

		for (EnumFacing dir : EnumFacing.values()) {
			TileEntity nearbyTile = robot.worldObj.getTileEntity(station.pos().offset(dir));

			if (nearbyTile != null && nearbyTile instanceof IInventory) {
				ITransactor trans = Transactor.getTransactorFor(nearbyTile);

				ItemStack added = trans.add(invSlot.getStackInSlot(), dir.getOpposite(), doInsert);

				if (doInsert) {
					invSlot.decreaseStackInSlot(added.stackSize);
				}

				return true;

			}
		}

		return false;
	}

	@Override
	public int getSheetLocation() {
		return 99;
	}
}
