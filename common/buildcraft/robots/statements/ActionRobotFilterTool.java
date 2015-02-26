/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.statements;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

import buildcraft.api.robots.IDockingStation;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.inventory.filters.ArrayStackOrListFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.inventory.filters.PassThroughStackFilter;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;
import buildcraft.robots.DockingStation;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.StatementSlot;

public class ActionRobotFilterTool extends BCStatement implements IActionInternal {

	public ActionRobotFilterTool() {
		super("buildcraft:robot.work_filter_tool");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.robot.filter_tool");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_robot_filter_tool");
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

	public static Collection<ItemStack> getGateFilterStacks(IDockingStation station) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (StatementSlot slot : new ActionIterator(((DockingStation) station).getPipe().pipe)) {
			if (slot.statement instanceof ActionRobotFilterTool) {
				for (IStatementParameter p : slot.parameters) {
					if (p != null && p instanceof StatementParameterItemStack) {
						StatementParameterItemStack param = (StatementParameterItemStack) p;
						ItemStack stack = param.getItemStack();

						if (stack != null) {
							result.add(stack);
						}
					}
				}
			}
		}

		return result;
	}

	public static IStackFilter getGateFilter(IDockingStation station) {
		Collection<ItemStack> stacks = getGateFilterStacks(station);

		if (stacks.size() == 0) {
			return new PassThroughStackFilter();
		} else {
			return new ArrayStackOrListFilter(stacks.toArray(new ItemStack[stacks.size()]));
		}
	}

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
	}
}
