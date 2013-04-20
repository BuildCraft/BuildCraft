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
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.IMachine;

public class TriggerMachine extends BCTrigger {

	boolean active;

	public TriggerMachine(int id, boolean active) {
		super(id);

		this.active = active;
	}

	@Override
	public String getDescription() {
		if (active)
			return "Work Scheduled";
		else
			return "Work Done";
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
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
	public int getIconIndex() {
		if (active)
			return ActionTriggerIconProvider.Trigger_Machine_Active;
		else
			return ActionTriggerIconProvider.Trigger_Machine_Inactive;
	}
}	
