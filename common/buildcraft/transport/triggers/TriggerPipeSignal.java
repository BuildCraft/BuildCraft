/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;
import java.util.Locale;

public class TriggerPipeSignal extends BCTrigger implements ITriggerPipe {

	boolean active;
	IPipe.WireColor color;

	public TriggerPipeSignal(int legacyId, boolean active, IPipe.WireColor color) {
		super(legacyId, "buildcraft.pipe.wire.input." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"));

		this.active = active;
		this.color = color;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		if (active) {
			switch (color) {
				case Red:
					return "Red Pipe Signal On";
				case Blue:
					return "Blue Pipe Signal On";
				case Green:
					return "Green Pipe Signal On";
				case Yellow:
					return "Yellow Pipe Signal On";
			}
		} else {
			switch (color) {
				case Red:
					return "Red Pipe Signal Off";
				case Blue:
					return "Blue Pipe Signal Off";
				case Green:
					return "Green Pipe Signal Off";
				case Yellow:
					return "Yellow Pipe Signal Off";
			}
		}

		return "";
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		if (active)
			return pipe.signalStrength[color.ordinal()] > 0;
		else
			return pipe.signalStrength[color.ordinal()] == 0;
	}

	@Override
	public int getIconIndex() {
		if (active) {
			switch (color) {
				case Red:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Red_Active;
				case Blue:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Active;
				case Green:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Green_Active;
				case Yellow:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Active;
			}
		} else {
			switch (color) {
				case Red:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Red_Inactive;
				case Blue:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Inactive;
				case Green:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Green_Inactive;
				case Yellow:
					return ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Inactive;
			}
		}
		return -1;
	}
}
