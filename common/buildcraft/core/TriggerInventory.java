/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public class TriggerInventory extends Trigger {

	public enum State {
		Empty, Contains, Space, Full
	};

	public State state;

	public TriggerInventory(int id, State state) {
		super(id);

		this.state = state;
	}

	@Override
	public int getIndexInTexture() {
		switch (state) {
		case Empty:
			return 2 * 16 + 4;
		case Contains:
			return 2 * 16 + 5;
		case Space:
			return 2 * 16 + 6;
		default:
			return 2 * 16 + 7;
		}
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
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		ItemStack searchedStack = null;

		if (parameter != null)
			searchedStack = parameter.getItem();

		if (tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0) {
			IInventory inv = Utils.getInventory(((IInventory) tile));

			boolean foundItems = false;
			boolean foundSpace = false;

			for (int i = 0; i < inv.getSizeInventory(); ++i) {
				ItemStack stack = inv.getStackInSlot(i);

				if (parameter == null || parameter.getItemStack() == null)
					foundItems = foundItems || stack != null && stack.stackSize > 0;
				else if (stack != null && stack.stackSize > 0)
					foundItems = foundItems
							|| (stack.itemID == parameter.getItemStack().itemID && stack.getItemDamage() == parameter.getItemStack()
									.getItemDamage());

				if (stack == null || stack.stackSize == 0)
					foundSpace = true;
				else if (searchedStack != null)
					if (stack.stackSize < stack.getMaxStackSize() && stack.itemID == searchedStack.itemID
							&& stack.getItemDamage() == searchedStack.getItemDamage())
						foundSpace = true;
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
	public String getTextureFile() {
		return DefaultProps.TEXTURE_TRIGGERS;
	}
}
