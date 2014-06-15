/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.IGate;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.TriggerParameterItemStack;
import buildcraft.transport.Pipe;

/**
 * This class has to be implemented to create new triggers kinds to BuildCraft
 * gates. There is an instance per kind, which will get called wherever the
 * trigger can be active.
 */
public abstract class BCTrigger extends BCStatement implements ITrigger {

	public BCTrigger(String... uniqueTag) {
		super(uniqueTag);
	}

	@Override
	public boolean isTriggerActive(IGate gate, ITriggerParameter[] parameters) {
		ITriggerParameter p = parameters[0];
		Pipe pipe = (Pipe) gate.getPipe();

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (isTriggerActive(side.getOpposite(), pipe.getAdjacentTile(side), p)) {
				return true;
			}
		}

		return false;
	}

	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		return false;
	}

	@Override
	public ITriggerParameter createParameter(int index) {
		return new TriggerParameterItemStack();
	}

}
