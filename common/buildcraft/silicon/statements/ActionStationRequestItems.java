/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import java.util.LinkedList;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IGate;
import buildcraft.core.triggers.BCActionActive;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;

public class ActionStationRequestItems extends BCActionActive {

	public ActionStationRequestItems() {
		super("buildcraft:station.request_items");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.request_items");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_request_items");
	}

	@Override
	public int maxParameters() {
		return 3;
	}

	@Override
	public IActionParameter createParameter(int index) {
		return new ActionParameterItemStack();
	}

	@Override
	public void actionActivate(IGate gate, IActionParameter[] parameters) {
		Pipe pipe = (Pipe) gate.getPipe();

		LinkedList<ItemStack> filter = new LinkedList<ItemStack>();

		for (IActionParameter p : parameters) {
			if (p != null && p instanceof ActionParameterItemStack) {
				ActionParameterItemStack param = (ActionParameterItemStack) p;

				if (param.getItemStackToDraw() != null) {
					filter.add(param.getItemStackToDraw());
				}
			}
		}

		pipe.pushActionState(new StateStationRequestItems(filter));
	}
}
