/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import java.util.Locale;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.utils.StringUtils;

public class TriggerInventory extends BCTrigger implements ITileTrigger {

	public enum State {

		Empty, Contains, Space, Full
	};
	public State state;

	public TriggerInventory(State state) {
		super("buildcraft:inventory." + state.name().toLowerCase(Locale.ENGLISH), "buildcraft.inventory." + state.name().toLowerCase(Locale.ENGLISH));

		this.state = state;
	}

	@Override
	public boolean hasParameter() {
		return state == State.Contains || state == State.Space;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize("gate.trigger.inventory." + state.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		ItemStack searchedStack = null;

		if (parameter != null) {
			searchedStack = parameter.getItemStack();
		}

		if (tile instanceof IInventory) {
			boolean hasSlots = false;
			boolean foundItems = false;
			boolean foundSpace = false;

			for (IInvSlot slot : InventoryIterator.getIterable((IInventory) tile, side)) {
				hasSlots = true;
				ItemStack stack = slot.getStackInSlot();

				foundItems |= stack != null && (searchedStack == null || StackHelper.canStacksMerge(stack, searchedStack));
				foundSpace |= (stack == null || (StackHelper.canStacksMerge(stack, searchedStack) && stack.stackSize < stack.getMaxStackSize()))
						&& (searchedStack == null || slot.canPutStackInSlot(searchedStack));
			}

			if (!hasSlots) {
				return false;
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

	@Override
	public ITrigger rotateLeft() {
		return this;
	}
}
