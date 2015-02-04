/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
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

import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.robots.IDockingStation;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.inventory.filters.ArrayFluidFilter;
import buildcraft.core.inventory.filters.ArrayStackOrListFilter;
import buildcraft.core.inventory.filters.IFluidFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.inventory.filters.PassThroughFluidFilter;
import buildcraft.core.inventory.filters.PassThroughStackFilter;
import buildcraft.core.inventory.filters.StatementParameterStackFilter;
import buildcraft.core.statements.BCStatement;
import buildcraft.core.utils.StringUtils;
import buildcraft.robots.DockingStation;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.StatementSlot;

public class ActionRobotFilter extends BCStatement implements IActionInternal {

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
	public IStatementParameter createParameter(int index) {
		return new StatementParameterItemStack();
	}

	public static Collection<ItemStack> getGateFilterStacks(IDockingStation station) {
		ArrayList<ItemStack> result = new ArrayList<ItemStack>();

		for (StatementSlot slot : new ActionIterator(((DockingStation) station).getPipe().pipe)) {
			if (slot.statement instanceof ActionRobotFilter) {
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

	public static IFluidFilter getGateFluidFilter(IDockingStation station) {
		Collection<ItemStack> stacks = getGateFilterStacks(station);

		if (stacks.size() == 0) {
			return new PassThroughFluidFilter();
		} else {
			return new ArrayFluidFilter(stacks.toArray(new ItemStack[stacks.size()]));
		}
	}

	public static boolean canInteractWithItem(DockingStation station, IStackFilter filter, Class<?> actionClass) {
		boolean actionFound = false;

		Pipe pipe = station.getPipe().pipe;

		for (StatementSlot s : new ActionIterator(pipe)) {
			if (actionClass.isAssignableFrom(s.statement.getClass())) {
				StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

				if (!param.hasFilter() || param.matches(filter)) {
					actionFound = true;
					break;
				}
			}
		}

		return actionFound;
	}

	public static boolean canInteractWithFluid(DockingStation station, IFluidFilter filter, Class<?> actionClass) {
		boolean actionFound = false;
		Pipe pipe = station.getPipe().pipe;

		for (StatementSlot s : new ActionIterator(pipe)) {
			if (actionClass.isAssignableFrom(s.statement.getClass())) {
				StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

				if (!param.hasFilter()) {
					actionFound = true;
					break;
				} else {
					for (ItemStack stack : param.getStacks()) {
						if (stack != null) {
							FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);

							if (fluid != null && filter.matches(fluid.getFluid())) {
								actionFound = true;
								break;
							}
						}
					}
				}
			}
		}

		return actionFound;

	}

	@Override
	public void actionActivate(IStatementContainer source,
			IStatementParameter[] parameters) {
		
	}
}
