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
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.inventory.filters.StatementParameterStackFilter;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.statements.BCStatement;
import buildcraft.transport.gates.StatementSlot;

public abstract class ActionStationInputItems extends BCStatement implements IActionInternal {

	public ActionStationInputItems(String name) {
		super(name);
	}

	public boolean insert(DockingStation station, EntityRobot robot, StatementSlot actionSlot, IInvSlot invSlot,
			boolean doInsert) {
		StatementParameterStackFilter param = new StatementParameterStackFilter(actionSlot.parameters);

		return !param.hasFilter() || param.matches(invSlot.getStackInSlot());
	}

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		
	}
}
