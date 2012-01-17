package net.minecraft.src.buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IOverrideDefaultTriggers;
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.ITriggerProvider;
import net.minecraft.src.buildcraft.api.Trigger;
import net.minecraft.src.buildcraft.transport.Pipe.GateKind;

public class PipeTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<Trigger> getPipeTriggers(IPipe iPipe) {
		if (iPipe instanceof IOverrideDefaultTriggers) {
			return ((IOverrideDefaultTriggers) iPipe).getTriggers();
		}
		
		LinkedList <Trigger> result = new LinkedList <Trigger> ();
		
		Pipe pipe = (Pipe) iPipe;
		
		if (pipe.wireSet [IPipe.WireColor.Red.ordinal()] && pipe.gateKind.ordinal() >= GateKind.AND_2.ordinal()) {
			result.add (BuildCraftTransport.triggerRedSignalActive);
			result.add (BuildCraftTransport.triggerRedSignalInactive);
		}
		
		if (pipe.wireSet [IPipe.WireColor.Blue.ordinal()] && pipe.gateKind.ordinal() >= GateKind.AND_3.ordinal()) {
			result.add (BuildCraftTransport.triggerBlueSignalActive);
			result.add (BuildCraftTransport.triggerBlueSignalInactive);
		}
		
		if (pipe.wireSet [IPipe.WireColor.Green.ordinal()] && pipe.gateKind.ordinal() >= GateKind.AND_4.ordinal()) {
			result.add (BuildCraftTransport.triggerGreenSignalActive);
			result.add (BuildCraftTransport.triggerGreenSignalInactive);
		}

		if (pipe.wireSet [IPipe.WireColor.Yellow.ordinal()] && pipe.gateKind.ordinal() >= GateKind.AND_4.ordinal()) {
			result.add (BuildCraftTransport.triggerYellowSignalActive);
			result.add (BuildCraftTransport.triggerYellowSignalInactive);
		}
		
		if (pipe.transport instanceof PipeTransportItems) {
			result.add (BuildCraftTransport.triggerPipeEmpty);
			result.add (BuildCraftTransport.triggerPipeItems);
		} else if (pipe.transport instanceof PipeTransportPower) {
			result.add (BuildCraftTransport.triggerPipeEmpty);
			result.add (BuildCraftTransport.triggerPipeEnergy);
		} else if (pipe.transport instanceof PipeTransportLiquids) {
			result.add (BuildCraftTransport.triggerPipeEmpty);
			result.add (BuildCraftTransport.triggerPipeLiquids);
		}
		
		return result;
	}

	@Override
	public LinkedList<Trigger> getNeighborTriggers(Block block, TileEntity tile) {
		return null;
	}

}
