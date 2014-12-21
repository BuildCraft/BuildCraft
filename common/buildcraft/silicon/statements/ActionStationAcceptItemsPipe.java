/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.gates.StatementSlot;

public class ActionStationAcceptItemsPipe extends ActionStationInputItems {

	public ActionStationAcceptItemsPipe() {
		super("buildcraft:station.drop_in_pipe");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.drop_items_in_pipe");
	}

	/*@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_drop_in_pipe");
	}*/

	@Override
	public int maxParameters() {
		return 3;
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

		if (!doInsert) {
			return true;
		}

		if (station.getPipe().pipe.transport instanceof PipeTransportItems) {
			float cx = station.pos().getX() + 0.5F + 0.2F * station.side().getFrontOffsetX();
			float cy = station.pos().getY() + 0.5F + 0.2F * station.side().getFrontOffsetY();
			float cz = station.pos().getZ() + 0.5F + 0.2F * station.side().getFrontOffsetZ();

			TravelingItem item = TravelingItem.make(cx, cy, cz, invSlot.getStackInSlot());

			((PipeTransportItems) station.getPipe().pipe.transport).injectItem(item, station.side().getOpposite());

			invSlot.setStackInSlot(null);

			return true;
		}

		return false;
	}

	@Override
	public int getSheetLocation() {
		// TODO Auto-generated method stub
		return 112;
	}

}
