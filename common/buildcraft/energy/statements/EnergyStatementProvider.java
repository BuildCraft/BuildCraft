package buildcraft.energy.statements;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.statements.IActionProvider;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.energy.TileEngine;

public class EnergyStatementProvider implements ITriggerProvider {

	@Override
	public Collection<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
		return null;
	}

	@Override
	public Collection<ITriggerExternal> getExternalTriggers(ForgeDirection side, TileEntity tile) {
		LinkedList<ITriggerExternal> triggers = new LinkedList<ITriggerExternal>();
		
		if (tile instanceof TileEngine) {
			triggers.add(BuildCraftEnergy.triggerBlueEngineHeat);
			triggers.add(BuildCraftEnergy.triggerGreenEngineHeat);
			triggers.add(BuildCraftEnergy.triggerYellowEngineHeat);
			triggers.add(BuildCraftEnergy.triggerRedEngineHeat);
		}
		
		return triggers;
	}
	
}
