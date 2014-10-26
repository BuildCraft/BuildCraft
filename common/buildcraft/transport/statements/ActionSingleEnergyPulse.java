/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import buildcraft.core.statements.BCActionPassive;
import buildcraft.core.utils.StringUtils;

public class ActionSingleEnergyPulse extends BCActionPassive {

	public ActionSingleEnergyPulse() {
		super("buildcraft:pulsar.single", "buildcraft.pulser.single");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.pulsar.single");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_single_pulsar");
	}
}
