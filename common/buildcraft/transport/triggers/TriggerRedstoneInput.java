/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.triggers;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerRedstoneInput extends BCTrigger implements ITriggerPipe {

	boolean active;

	public TriggerRedstoneInput(int id, boolean active) {
		super(id);

		this.active = active;
	}

	@Override
	public String getDescription() {
		if (active)
			return "Redstone Signal On";
		else
			return "Redstone Signal Off";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (active)
			return pipe.worldObj.isBlockIndirectlyGettingPowered(pipe.xCoord, pipe.yCoord, pipe.zCoord);
		else
			return !pipe.worldObj.isBlockIndirectlyGettingPowered(pipe.xCoord, pipe.yCoord, pipe.zCoord);
	}
	
	@Override
	public int getIconIndex() {
		if (active)
			return ActionTriggerIconProvider.Trigger_RedstoneInput_Active;
		else
			return ActionTriggerIconProvider.Trigger_RedstoneInput_Inactive;
	}
}
