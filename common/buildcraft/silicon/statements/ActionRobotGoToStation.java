/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import buildcraft.api.gates.IAction;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;

public class ActionRobotGoToStation extends BCAction {

	private IIcon icon;

	public ActionRobotGoToStation() {
		super("buildcraft:robot.goto_station");
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.robot.goto_station");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_robot_goto_station");
	}

	@Override
	public IAction rotateLeft() {
		return this;
	}

}
