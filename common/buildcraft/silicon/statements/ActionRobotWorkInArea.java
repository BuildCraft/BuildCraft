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
import buildcraft.api.core.IZone;
import buildcraft.api.gates.StatementParameterItemStack;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.statements.BCActionPassive;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.gates.ActionSlot;

public class ActionRobotWorkInArea extends BCActionPassive {

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

	public static IZone getArea(ActionSlot slot) {
		if (slot.parameters[0] == null) {
			return null;
		}

		ItemStack stack = ((StatementParameterItemStack) slot.parameters[0]).getItemStack();

		if (!(stack.getItem() instanceof ItemMapLocation)) {
			return null;
		}

		return ItemMapLocation.getZone(stack);
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
}
