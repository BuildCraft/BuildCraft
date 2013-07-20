/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.triggers;


public class ActionRedstoneOutput extends BCAction {

	public ActionRedstoneOutput(int legacyId) {
		super(legacyId, "buildcraft.redstone.output");
	}

	@Override
	public String getDescription() {
		return "Redstone Signal";
	}

	@Override
	public int getIconIndex() {
		return ActionTriggerIconProvider.Trigger_RedstoneInput_Active;
	}
}
