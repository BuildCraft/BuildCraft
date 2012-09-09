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
import buildcraft.api.gates.Trigger;
import buildcraft.core.DefaultProps;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerRedstoneInput extends Trigger implements ITriggerPipe {

	boolean active;

	public TriggerRedstoneInput(int id, boolean active) {
		super(id);

		this.active = active;
	}

	@Override
	public int getIndexInTexture() {
		if (active)
			return 0 * 16 + 0;
		else
			return 0 * 16 + 1;
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
	public String getTextureFile() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}
}
