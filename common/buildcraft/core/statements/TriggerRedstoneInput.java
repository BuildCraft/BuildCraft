/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.statements;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.TileGenericPipe;

public class TriggerRedstoneInput extends BCStatement implements ITriggerInternal {

	boolean active;

	public TriggerRedstoneInput(boolean active) {
		super("buildcraft:redstone.input." + (active ? "active" : "inactive"), active ? "buildcraft.redtone.input.active" : "buildcraft.redtone.input.inactive");
		this.active = active;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.redstone.input." + (active ? "active" : "inactive"));
	}
	
    @Override
    public IStatementParameter createParameter(int index) {
		IStatementParameter param = null;
	
		if (index == 0) {
		    param = new StatementParameterRedstoneGateSideOnly();
		}
	
		return param;
    }

	@Override
	public int getSheetLocation() {
		return 15 + (active ? 0 : 1) * 16;
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
		if (!(container.getTile() instanceof TileGenericPipe)) {
			return false;
		}
		
		TileGenericPipe tile = (TileGenericPipe) container.getTile();
		int level = tile.redstoneInput;
		if (parameters.length > 0 && parameters[0] instanceof StatementParameterRedstoneGateSideOnly &&
				((StatementParameterRedstoneGateSideOnly) parameters[0]).isOn) {
			level = tile.redstoneInputSide[((IGate) container).getSide().ordinal()];
		}
		
		return active ? level > 0 : level == 0;
	}
}
