/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import java.util.Locale;

import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.IPipeTrigger;
import buildcraft.transport.Pipe;

public class TriggerPipeSignal extends BCTrigger implements IPipeTrigger {

	boolean active;
	PipeWire color;

	public TriggerPipeSignal(boolean active, PipeWire color) {
		super("buildcraft:pipe.wire.input." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"),
				"buildcraft.pipe.wire.input." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"));

		this.active = active;
		this.color = color;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.trigger.pipe.wire." + (active ? "active" : "inactive")), StringUtils.localize("color." + color.name().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (active) {
			return pipe.signalStrength[color.ordinal()] > 0;
		} else {
			return pipe.signalStrength[color.ordinal()] == 0;
		}
	}

	@Override
	public int getIconIndex() {
		if (active) {
			switch (color) {
				case RED:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Red_Active;
				case BLUE:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Active;
				case GREEN:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Green_Active;
				case YELLOW:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Active;
			}
		} else {
			switch (color) {
				case RED:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Red_Inactive;
				case BLUE:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Inactive;
				case GREEN:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Green_Inactive;
				case YELLOW:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Inactive;
			}
		}
		return -1;
	}

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
