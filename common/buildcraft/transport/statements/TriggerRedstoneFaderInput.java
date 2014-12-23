/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.statements;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.statements.StatementParameterRedstoneGateSideOnly;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.TileGenericPipe;

public class TriggerRedstoneFaderInput extends BCStatement implements ITriggerInternal {

	public final int level;

	public TriggerRedstoneFaderInput(int level) {
		super(String.format("buildcraft:redtone.input.%02d", level));

		this.level = level;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.redstone.input.level"), level);
	}

	@Override
	public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
		if (!(container instanceof IGate)) {
			return false;
		}
		
		IGate gate = (IGate) container;
		TileGenericPipe tile = (TileGenericPipe) gate.getPipe().getTile();
		int inputLevel = tile.redstoneInput;
		if (parameters.length > 0 && parameters[0] instanceof StatementParameterRedstoneGateSideOnly &&
				((StatementParameterRedstoneGateSideOnly) parameters[0]).isOn) {
			inputLevel = tile.redstoneInputSide[gate.getSide().ordinal()];
		}
		
		return inputLevel == level;
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
		return 9 + level * 16;
	}

	@Override
	public int maxParameters() {
		return 1;
	}
}
