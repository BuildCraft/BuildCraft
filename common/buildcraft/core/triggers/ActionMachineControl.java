/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import java.util.Locale;

import buildcraft.api.gates.IAction;
import buildcraft.core.utils.StringUtils;

public class ActionMachineControl extends BCAction {

	public enum Mode {

		Unknown, On, Off, Loop
	};
	public final Mode mode;

	public ActionMachineControl(Mode mode) {
		super("buildcraft:machine." + mode.name().toLowerCase(Locale.ENGLISH), "buildcraft.machine." + mode.name().toLowerCase(Locale.ENGLISH));

		this.mode = mode;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.machine." + mode.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public int getIconIndex() {
		switch (mode) {
			case On:
				return ActionTriggerIconProvider.Action_MachineControl_On;
			case Off:
				return ActionTriggerIconProvider.Action_MachineControl_Off;
			case Loop:
			default:
				return ActionTriggerIconProvider.Action_MachineControl_Loop;
		}
	}

	@Override
	public IAction rotateLeft() {
		return this;
	}
}
