/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.IMachine;
import buildcraft.core.utils.StringUtils;

public class TriggerMachine extends BCTrigger implements ITileTrigger {

	boolean active;

	public TriggerMachine(boolean active) {
		super("buildcraft:work." + (active ? "scheduled" : "done"), "buildcraft.work." + (active ? "scheduled" : "done"));

		this.active = active;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.machine." + (active ? "scheduled" : "done"));
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if (tile instanceof IMachine) {
			IMachine machine = (IMachine) tile;

			if (active) {
				return machine.isActive();
			} else {
				return !machine.isActive();
			}
		}

		return false;
	}

	@Override
	public int getIconIndex() {
		if (active) {
			return ActionTriggerIconProvider.Trigger_Machine_Active;
		} else {
			return ActionTriggerIconProvider.Trigger_Machine_Inactive;
		}
	}

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
