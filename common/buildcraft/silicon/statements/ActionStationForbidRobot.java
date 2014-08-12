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
import net.minecraft.item.ItemStack;

import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.ItemRobot;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.triggers.BCActionPassive;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

public class ActionStationForbidRobot extends BCActionPassive {

	public ActionStationForbidRobot() {
		super("buildcraft:station.forbid_robot");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.station.forbid_robot");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_station_robot_forbidden");
	}

	@Override
	public int minParameters() {
		return 1;
	}

	@Override
	public int maxParameters() {
		return 3;
	}

	@Override
	public IActionParameter createParameter(int index) {
		return new ActionParameterItemStack();
	}

	public static boolean isForbidden(DockingStation station, EntityRobotBase robot) {
		for (ActionSlot s : new ActionIterator(station.getPipe().pipe)) {
			if (s.action instanceof ActionStationForbidRobot) {
				if (ActionStationForbidRobot.isForbidden(s, robot)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isForbidden(ActionSlot slot, EntityRobotBase robot) {
		for (IActionParameter p : slot.parameters) {
			if (p != null) {
				ActionParameterItemStack actionStack = (ActionParameterItemStack) p;
				ItemStack stack = p.getItemStackToDraw();

				if (stack != null && stack.getItem() instanceof ItemRobot) {
					return ItemRobot.getRobotNBT(stack) == robot.getBoard().getNBTHandler();
				}
			}
		}

		return false;
	}
}
