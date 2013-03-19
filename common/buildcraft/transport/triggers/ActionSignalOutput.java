/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.triggers;

import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IPipe;
import buildcraft.core.DefaultProps;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;

public class ActionSignalOutput extends BCAction {

	public IPipe.WireColor color;

	public ActionSignalOutput(int id, IPipe.WireColor color) {
		super(id);

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
	@SideOnly(Side.CLIENT)
	public Icon getTexture() {
		switch (color) {
		case Red:
			return getIconProvider().getIcon(ActionTriggerIconProvider.Trigger_PipeSignal_Red_Active);
		case Blue:
			return getIconProvider().getIcon(ActionTriggerIconProvider.Trigger_PipeSignal_Blue_Active);
		case Green:
			return getIconProvider().getIcon(ActionTriggerIconProvider.Trigger_PipeSignal_Green_Active);
		case Yellow:
			return getIconProvider().getIcon(ActionTriggerIconProvider.Trigger_PipeSignal_Yellow_Active);
		}

		return null;
	}
}
