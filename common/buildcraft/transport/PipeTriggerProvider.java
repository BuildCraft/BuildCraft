package buildcraft.transport;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipe;
import buildcraft.transport.pipes.PipePowerWood;
import java.util.LinkedList;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public class PipeTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipe iPipe) {
		if (iPipe instanceof IOverrideDefaultTriggers)
			return ((IOverrideDefaultTriggers) iPipe).getTriggers();

		LinkedList<ITrigger> result = new LinkedList<ITrigger>();

		Pipe pipe = (Pipe) iPipe;

		if (pipe.hasGate()) {
			pipe.gate.addTrigger(result);
		}

		if (pipe.transport instanceof PipeTransportItems) {
			result.add(BuildCraftTransport.triggerPipeEmpty);
			result.add(BuildCraftTransport.triggerPipeItems);
		} else if (pipe.transport instanceof PipeTransportPower) {
			result.add(BuildCraftTransport.triggerPipeEmpty);
			result.add(BuildCraftTransport.triggerPipeContainsEnergy);
			result.add(BuildCraftTransport.triggerPipeTooMuchEnergy);
			if (pipe instanceof PipePowerWood) {
				result.add(BuildCraftTransport.triggerPipeRequestsEnergy);
			}
		} else if (pipe.transport instanceof PipeTransportFluids) {
			result.add(BuildCraftTransport.triggerPipeEmpty);
			result.add(BuildCraftTransport.triggerPipeFluids);
		}

		return result;
	}

	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		return null;
	}
}
