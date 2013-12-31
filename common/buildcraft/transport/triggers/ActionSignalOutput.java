/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.utils.StringUtils;
import java.util.Locale;

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
