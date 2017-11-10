/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

import buildcraft.api.statements.ActionState;
import buildcraft.core.lib.inventory.filters.IStackFilter;

public class StateStationProvideItems extends ActionState {

	LinkedList<ItemStack> items;

	public StateStationProvideItems(LinkedList<ItemStack> filter) {
		items = filter;
	}

	public boolean matches(IStackFilter filter) {
		if (items.size() == 0) {
			return true;
		} else {
			for (ItemStack stack : items) {
				if (filter.matches(stack)) {
					return true;
				}
			}
		}

		return false;
	}

}
