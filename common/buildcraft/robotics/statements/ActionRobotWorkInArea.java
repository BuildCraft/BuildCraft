/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.gates.StatementSlot;

public class ActionRobotWorkInArea extends BCStatement implements IActionInternal {

	public ActionRobotWorkInArea() {
		super("buildcraft:robot.work_in_area");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.robot.work_in_area");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_robot_in_area");
	}

	public static IZone getArea(StatementSlot slot) {
		if (slot.parameters[0] == null) {
			return null;
		}

		ItemStack stack = slot.parameters[0].getItemStack();

		if (stack == null || !(stack.getItem() instanceof IMapLocation)) {
			return null;
		}

		IMapLocation map = (IMapLocation) stack.getItem();
		return map.getZone(stack);
	}

	@Override
	public int minParameters() {
		return 1;
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		
	}
}
