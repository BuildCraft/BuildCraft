/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftCore;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.statements.TriggerPipeContents;

public class PipeTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipeTile tile) {
		LinkedList<ITrigger> result = new LinkedList<ITrigger>();
		Pipe<?> pipe = null;
		if (tile instanceof TileGenericPipe) {
			pipe = ((TileGenericPipe) tile).pipe;
		}

		if (pipe == null) {
			return result;
		}
		
		boolean containsGate = false;

		for (Gate gate : pipe.gates) {
			if (gate != null) {
				containsGate = true;
				gate.addTriggers(result);
			}
		}
		
		result.add(BuildCraftCore.triggerRedstoneActive);
		result.add(BuildCraftCore.triggerRedstoneInactive);

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
				result.add(TriggerPipeContents.PipeContents.requestsEnergy.trigger);
				break;
			case STRUCTURE:
				break;
		}

		if (tile instanceof IEnergyHandler && ((IEnergyHandler) tile).getMaxEnergyStored(ForgeDirection.UNKNOWN) > 0) {
			result.add(BuildCraftCore.triggerEnergyHigh);
			result.add(BuildCraftCore.triggerEnergyLow);
		}

		return result;
	}

	@Override
	public LinkedList<ITrigger> getNeighborTriggers(ForgeDirection side, Block block, TileEntity tile) {
		return null;
	}
}
