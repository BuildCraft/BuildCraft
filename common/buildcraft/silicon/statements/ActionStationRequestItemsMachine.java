/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import net.minecraft.client.renderer.texture.IIconRegister;

import buildcraft.core.triggers.BCActionPassive;
import buildcraft.core.utils.StringUtils;

public class ActionStationRequestItemsMachine extends BCActionPassive {

	public ActionStationRequestItemsMachine() {
		super("buildcraft:station.provide_machine_request");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.provide_machine_request");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_machine_request");
	}
}
