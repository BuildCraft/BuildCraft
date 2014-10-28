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
import buildcraft.api.gates.StatementParameterItemStack;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.core.statements.BCActionPassive;
import buildcraft.core.utils.StringUtils;

public class ActionStationAcceptFluids extends BCActionPassive {

	public ActionStationAcceptFluids() {
		super("buildcraft:station.accept_fluids");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.accept_fluids");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_accept_fluids");
	}

	@Override
	public int maxParameters() {
		return 3;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}
}
