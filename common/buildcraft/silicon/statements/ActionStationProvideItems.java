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
import net.minecraft.util.IIcon;

import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IGate;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;

public class ActionStationProvideItems extends BCAction {

	private IIcon icon;

	public ActionStationProvideItems() {
		super("buildcraft:station.provide_items");
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.provide_items");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_provide_items");
	}

	@Override
	public IAction rotateLeft() {
		return this;
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

		pipe.pushActionState(new StateStationProvideItems(filter));
	}
}
