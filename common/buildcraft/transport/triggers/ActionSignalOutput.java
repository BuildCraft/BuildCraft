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

import buildcraft.api.gates.IAction;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;

public class ActionSignalOutput extends BCAction {

	public PipeWire color;

	public ActionSignalOutput(PipeWire color) {
		super("buildcraft:pipe.wire.output." + color.name().toLowerCase(Locale.ENGLISH), "buildcraft.pipe.wire.output." + color.name().toLowerCase(Locale.ENGLISH));

		this.color = color;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.action.pipe.wire"), StringUtils.localize("color." + color.name().toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public int getIconIndex() {
		switch (color) {
			case RED:
				return ActionTriggerIconProvider.Trigger_PipeSignal_Red_Active;
			case BLUE:
				return ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Active;
			case GREEN:
				return ActionTriggerIconProvider.Trigger_PipeSignal_Green_Active;
			case YELLOW:
			default:
				return ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Active;
		}
	}

	@Override
	public IAction rotateLeft() {
		return this;
	}
}
