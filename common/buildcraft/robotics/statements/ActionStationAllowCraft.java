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

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.lib.utils.StringUtils;

public class ActionStationAllowCraft extends BCStatement implements IActionInternal {

	public ActionStationAllowCraft() {
		super("buildcraft:robot.allow_craft");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.allow_craft");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_allow_craft");
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
}
