package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.pipes.PipePowerWood;
import java.util.LinkedList;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public class PipeTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipeTile tile) {
		LinkedList<ITrigger> result = new LinkedList<ITrigger>();
		Pipe pipe = null;
		if (tile instanceof TileGenericPipe)
			pipe = ((TileGenericPipe) tile).pipe;
		if (pipe == null)
			return result;
		if (pipe instanceof IOverrideDefaultTriggers)
			return ((IOverrideDefaultTriggers) pipe).getTriggers();

		if (pipe.hasGate())
			pipe.gate.addTrigger(result);

		switch (tile.getPipeType()) {
			case ITEM:
				result.add(BuildCraftTransport.triggerPipeEmpty);
				result.add(BuildCraftTransport.triggerPipeItems);
				break;
			case FLUID:
				result.add(BuildCraftTransport.triggerPipeEmpty);
				result.add(BuildCraftTransport.triggerPipeFluids);
				break;
			case POWER:
				result.add(BuildCraftTransport.triggerPipeEmpty);
				result.add(BuildCraftTransport.triggerPipeContainsEnergy);
				result.add(BuildCraftTransport.triggerPipeTooMuchEnergy);
				if (pipe instanceof PipePowerWood) {
					result.add(BuildCraftTransport.triggerPipeRequestsEnergy);
				}
				break;
		}

		return result;
	}

	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		return null;
	}
}
