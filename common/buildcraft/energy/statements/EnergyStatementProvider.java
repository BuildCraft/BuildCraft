package buildcraft.energy.statements;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.energy.TileEngine;

public class EnergyStatementProvider implements ITriggerProvider {

	@Override
	public Collection<ITrigger> getPipeTriggers(IPipeTile pipe) {
		return null;
	}

	@Override
	public Collection<ITrigger> getNeighborTriggers(ForgeDirection side, Block block, TileEntity tile) {
		LinkedList<ITrigger> triggers = new LinkedList<ITrigger>();
		
		if (tile instanceof TileEngine) {
			triggers.add(BuildCraftEnergy.triggerBlueEngineHeat);
			triggers.add(BuildCraftEnergy.triggerGreenEngineHeat);
			triggers.add(BuildCraftEnergy.triggerYellowEngineHeat);
			triggers.add(BuildCraftEnergy.triggerRedEngineHeat);
		}
		
		return triggers;
	}
	
}
