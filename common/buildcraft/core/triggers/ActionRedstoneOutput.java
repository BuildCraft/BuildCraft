/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import buildcraft.api.gates.IAction;

public class ActionRedstoneOutput extends BCAction {

	public ActionRedstoneOutput() {
		super("buildcraft:redstone.output", "buildcraft.redstone.output");
	}

	@Override
	public String getDescription() {
		return "Redstone Signal";
	}

	@Override
	public int getIconIndex() {
		return ActionTriggerIconProvider.Trigger_RedstoneInput_Active;
	}

	@Override
	public IAction rotateLeft() {
		return this;
	}
}
