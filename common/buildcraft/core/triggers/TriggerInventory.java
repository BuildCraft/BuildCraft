/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.triggers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.utils.SidedInventoryAdapter;
import buildcraft.core.utils.Utils;

public class TriggerInventory extends BCTrigger {

	public enum State {
		Empty, Contains, Space, Full
	};

	public State state;

	public TriggerInventory(int id, State state) {
		super(id);

		this.state = state;
	}

	@Override
	public boolean hasParameter() {
		if (state == State.Contains || state == State.Space)
			return true;
		else
			return false;
	}

	@Override
	public String getDescription() {
		switch (state) {
		case Empty:
			return "Inventory Empty";
		case Contains:
			return "Items in Inventory";
		case Space:
			return "Space in Inventory";
		default:
			return "Inventory Full";
		}
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		ItemStack searchedStack = null;

		if (parameter != null) {
			searchedStack = parameter.getItem();
		}

		if (tile instanceof IInventory) {
			IInventory inv = Utils.getInventory(((IInventory) tile));
			if (side != ForgeDirection.UNKNOWN && inv instanceof ISidedInventory) {
				inv = new SidedInventoryAdapter((ISidedInventory) inv, side);
			}

			int invSize = inv.getSizeInventory();

			if (invSize <= 0)
				return false;

			boolean foundItems = false;
			boolean foundSpace = false;

			for (int i = 0; i < invSize; ++i) {
				ItemStack stack = inv.getStackInSlot(i);

				boolean slotEmpty = stack == null || stack.stackSize == 0;

				if (searchedStack == null) {
					foundItems |= !slotEmpty;
				} else if (!slotEmpty) {
					foundItems |= stack.isItemEqual(searchedStack);
				}

				if (slotEmpty) {
					foundSpace = true;
				} else if (searchedStack != null) {
					if (stack.stackSize < stack.getMaxStackSize() && stack.isItemEqual(searchedStack)) {
						foundSpace = true;
					}
				}
			}

			switch (state) {
			case Empty:
				return !foundItems;
			case Contains:
				return foundItems;
			case Space:
				return foundSpace;
			default:
				return !foundSpace;
			}
		}

		return false;
	}

	@Override
	public int getIconIndex() {
		switch (state) {
		case Empty:
			return ActionTriggerIconProvider.Trigger_Inventory_Empty;
		case Contains:
			return ActionTriggerIconProvider.Trigger_Inventory_Contains;
		case Space:
			return ActionTriggerIconProvider.Trigger_Inventory_Space;
		default:
			return ActionTriggerIconProvider.Trigger_Inventory_Full;
		}
	}
}
