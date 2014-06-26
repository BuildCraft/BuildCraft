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

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IBox;
import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IGate;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class ActionRobotWorkInArea extends BCAction {

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

	@Override
	public void actionActivate(IGate gate, IActionParameter[] parameters) {
		if (parameters[0] == null) {
			return;
		}

		ItemStack stack = ((ActionParameterItemStack) parameters[0]).getItemStackToDraw();

		if (!(stack.getItem() instanceof ItemMapLocation)) {
			return;
		}

		IBox box = ItemMapLocation.getBlox(stack);

		if (box == null) {
			return;
		}

		Pipe<?> pipe = (Pipe<?>) gate.getPipe();
		TileGenericPipe tile = pipe.container;

		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
			DockingStation station = tile.getStation(d);

			if (station != null && station.linked() != null) {
				station.linked().workInArea(box);
			}
		}
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public IActionParameter createParameter(int index) {
		return new ActionParameterItemStack();
	}
}
