/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.EntityRobot;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

public class ActionStationRequestItems extends ActionStationInputItems {

	public ActionStationRequestItems() {
		super("buildcraft:station.request_items");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.request_items");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraftrobotics:triggers/action_station_request_items");
	}

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

		if (((Pipe) station.getPipe().getPipe()).transport instanceof PipeTransportItems) {
			float cx = station.x() + 0.5F + 0.2F * station.side().offsetX;
			float cy = station.y() + 0.5F + 0.2F * station.side().offsetY;
			float cz = station.z() + 0.5F + 0.2F * station.side().offsetZ;

			TravelingItem item = TravelingItem.make(cx, cy, cz, invSlot.getStackInSlot());

			((PipeTransportItems) ((Pipe) station.getPipe().getPipe()).transport).injectItem(item, station.side().getOpposite());

			invSlot.setStackInSlot(null);

			return true;
		}

		return false;
	}
}
