/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.statements;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.robots.IDockingStation;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.inventory.filters.PassThroughStackFilter;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.triggers.BCActionPassive;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

public class ActionRobotFilter extends BCActionPassive {

	public ActionRobotFilter() {
		super("buildcraft:robot.work_filter");
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.action.robot.filter");
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/action_robot_filter");
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

	public static Collection<ItemStack> getGateFilterStacks(IDockingStation station) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (ActionSlot slot : new ActionIterator(((DockingStation) station).pipe.pipe)) {
			if (slot.action instanceof ActionRobotFilter) {
				for (IActionParameter p : slot.parameters) {
					if (p != null && p instanceof ActionParameterItemStack) {
						ActionParameterItemStack param = (ActionParameterItemStack) p;
						ItemStack stack = param.getItemStackToDraw();

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
			return new ArrayStackFilter(stacks.toArray(new ItemStack[stacks.size()]));
		}
	}
}
