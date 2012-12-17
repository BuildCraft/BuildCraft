package buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipe;

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
			result.add(BuildCraftTransport.triggerPipeEnergy);
		} else if (pipe.transport instanceof PipeTransportLiquids) {
			result.add(BuildCraftTransport.triggerPipeEmpty);
			result.add(BuildCraftTransport.triggerPipeLiquids);
		}

		return result;
	}

	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		return null;
	}

}
