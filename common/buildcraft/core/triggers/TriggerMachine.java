/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.triggers;

import net.minecraft.tileentity.TileEntity;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import buildcraft.core.DefaultProps;
import buildcraft.core.IMachine;

public class TriggerMachine extends Trigger {

	boolean active;

	public TriggerMachine(int id, boolean active) {
		super(id);

		this.active = active;
	}

	@Override
	public int getIndexInTexture() {
		if (active)
			return 4 * 16 + 0;
		else
			return 4 * 16 + 1;
	}

	@Override
	public String getDescription() {
		if (active)
			return "Work Scheduled";
		else
			return "Work Done";
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof IMachine) {
			IMachine machine = (IMachine) tile;

			if (active)
				return machine.isActive();
			else
				return !machine.isActive();
		}

		return false;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}
}
