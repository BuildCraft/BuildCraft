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
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.ItemRobot;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.StatementSlot;

public class ActionStationForbidRobot extends BCStatement implements IActionInternal {

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
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}

	public static boolean isForbidden(DockingStation station, EntityRobotBase robot) {
		for (StatementSlot s : new ActionIterator(station.getPipe().pipe)) {
			if (s.statement instanceof ActionStationForbidRobot) {
				if (ActionStationForbidRobot.isForbidden(s, robot)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isForbidden(StatementSlot slot, EntityRobotBase robot) {
		for (IStatementParameter p : slot.parameters) {
			if (p != null) {
				StatementParameterItemStack actionStack = (StatementParameterItemStack) p;
				ItemStack stack = p.getItemStack();

				if (stack != null && stack.getItem() instanceof ItemRobot) {
					return ItemRobot.getRobotNBT(stack) == robot.getBoard().getNBTHandler();
				}
			}
		}

		return false;
	}

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		
	}
}
