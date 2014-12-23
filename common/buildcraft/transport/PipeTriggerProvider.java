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

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import cofh.api.energy.IEnergyHandler;
import buildcraft.BuildCraftCore;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.transport.statements.TriggerPipeContents;

public class PipeTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
		LinkedList<ITriggerInternal> result = new LinkedList<ITriggerInternal>();
		Pipe<?> pipe = null;
		TileEntity tile = container.getTile();
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

		switch (((TileGenericPipe) tile).getPipeType()) {
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

		if (tile instanceof IEnergyHandler && ((IEnergyHandler) tile).getMaxEnergyStored(null) > 0) {
			result.add((ITriggerInternal) BuildCraftCore.triggerEnergyHigh);
			result.add((ITriggerInternal) BuildCraftCore.triggerEnergyLow);
		}

		return result;
	}

	@Override
	public LinkedList<ITriggerExternal> getExternalTriggers(EnumFacing side, TileEntity tile) {
		return null;
	}
}
