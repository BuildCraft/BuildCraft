/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.triggers;

import buildcraft.api.transport.IPipe;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import java.util.Locale;

public class ActionSignalOutput extends BCAction {

	public IPipe.WireColor color;

	public ActionSignalOutput(int id, IPipe.WireColor color) {
		super(id, "buildcraft.pipe.wire.output." + color.name().toLowerCase(Locale.ENGLISH));

		this.color = color;
	}

	@Override
	public String getDescription() {
		switch (color) {
		case Red:
			return "Red Pipe Signal";
		case Blue:
			return "Blue Pipe Signal";
		case Green:
			return "Green Pipe Signal";
		case Yellow:
			return "Yellow Pipe Signal";
		}

		return "";
	}

	@Override
	public int getIconIndex() {
		switch (color) {
		case Red:
			return ActionTriggerIconProvider.Trigger_PipeSignal_Red_Active;
		case Blue:
			return ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Active;
		case Green:
			return ActionTriggerIconProvider.Trigger_PipeSignal_Green_Active;
		case Yellow:
		default:
			return ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Active;
		}
	}
}
