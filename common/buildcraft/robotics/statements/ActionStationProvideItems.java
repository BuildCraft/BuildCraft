/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.lib.inventory.filters.StatementParameterStackFilter;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;

public class ActionStationProvideItems extends BCStatement implements IActionInternal {

	public ActionStationProvideItems() {
		super("buildcraft:station.provide_items");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.provide_items");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraftrobotics:triggers/action_station_provide_items");
	}

	@Override
	public int maxParameters() {
		return 3;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}

	@Override
	public void actionActivate(IStatementContainer source,
							   IStatementParameter[] parameters) {

	}

	public static boolean canExtractItem(DockingStation station, ItemStack stack) {
		boolean hasFilter = false;

		for (StatementSlot s : station.getActiveActions()) {
			if (s.statement instanceof ActionStationProvideItems) {
				StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

				if (param.hasFilter()) {
					hasFilter = true;

					if (param.matches(stack)) {
						return true;
					}
				}
			}
		}

		return !hasFilter;
	}
}
