/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.statements;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.energy.TileEngine;

public class EnergyStatementProvider implements ITriggerProvider {

	@Override
	public Collection<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
		return null;
	}

	@Override
	public Collection<ITriggerExternal> getExternalTriggers(EnumFacing side, TileEntity tile) {
		LinkedList<ITriggerExternal> triggers = new LinkedList<ITriggerExternal>();
		
		if (tile instanceof TileEngine) {
			triggers.add(BuildCraftEnergy.triggerBlueEngineHeat);
			triggers.add(BuildCraftEnergy.triggerGreenEngineHeat);
			triggers.add(BuildCraftEnergy.triggerYellowEngineHeat);
			triggers.add(BuildCraftEnergy.triggerRedEngineHeat);
			triggers.add(BuildCraftEnergy.triggerEngineOverheat);
		}
		
		return triggers;
	}
	
}
