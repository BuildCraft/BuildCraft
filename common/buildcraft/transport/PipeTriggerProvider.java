package buildcraft.transport;

import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.pipes.PipePowerWood;
import buildcraft.transport.triggers.TriggerPipeContents;
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
				result.add(TriggerPipeContents.PipeContents.empty.trigger);
				result.add(TriggerPipeContents.PipeContents.containsItems.trigger);
				break;
			case FLUID:
				result.add(TriggerPipeContents.PipeContents.empty.trigger);
				result.add(TriggerPipeContents.PipeContents.containsFluids.trigger);
				break;
			case POWER:
				result.add(TriggerPipeContents.PipeContents.empty.trigger);
				result.add(TriggerPipeContents.PipeContents.containsEnergy.trigger);
				result.add(TriggerPipeContents.PipeContents.tooMuchEnergy.trigger);
				if (pipe instanceof PipePowerWood) {
					result.add(TriggerPipeContents.PipeContents.requestsEnergy.trigger);
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
