/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.IGate;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;

public class TriggerRedstoneInput extends BCTrigger {

	boolean active;

	public TriggerRedstoneInput(boolean active) {
		super("buildcraft:redstone.input." + (active ? "active" : "inactive"), active ? "buildcraft.redtone.input.active" : "buildcraft.redtone.input.inactive");
		this.active = active;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.redstone.input." + (active ? "active" : "inactive"));
	}

	@Override
	public boolean isTriggerActive(IGate gate, ITriggerParameter[] parameters) {
		return !(active ^ isBeingPowered((Pipe) gate.getPipe(), gate.getSide()));
	}

	private boolean isBeingPowered(Pipe pipe, ForgeDirection direction) {
		return direction != ForgeDirection.UNKNOWN && pipe.container.redstoneInput[direction.ordinal()] > 0;
	}

	@Override
	public int getIconIndex() {
		return active ? StatementIconProvider.Trigger_RedstoneInput_Active : StatementIconProvider.Trigger_RedstoneInput_Inactive;
	}

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
