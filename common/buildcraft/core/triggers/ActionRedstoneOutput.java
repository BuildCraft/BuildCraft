/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.triggers;

import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.BuildCraftTransport;
import buildcraft.core.DefaultProps;

public class ActionRedstoneOutput extends BCAction {

	public ActionRedstoneOutput(int id) {
		super(id);
	}

	@Override
	public String getDescription() {
		return "Redstone Signal";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getTexture() {
		return getIconProvider().getIcon(ActionTriggerIconProvider.Trigger_RedstoneInput_Active);
	}
}
