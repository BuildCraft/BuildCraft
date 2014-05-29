/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.boards;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.recipes.IAssemblyRecipe;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.utils.NBTUtils;

public class BoardRecipe implements IAssemblyRecipe {

	private ItemStack[] inputs;
	private ItemStack output;

	public BoardRecipe () {
		inputs = new ItemStack[] {
				new ItemStack(BuildCraftSilicon.redstoneBoard)};

		output = new ItemStack(BuildCraftSilicon.redstoneBoard);
		NBTUtils.getItemData(output).setString("id", "<unknown>");
	}

	@Override
	public ItemStack getOutput() {
		return output;
	}

	@Override
	public ItemStack makeOutput() {
		ItemStack stack = new ItemStack(BuildCraftSilicon.redstoneBoard);
		RedstoneBoardRegistry.instance.createRandomBoard(NBTUtils.getItemData(stack));

		return stack;
	}

	@Override
	public Object[] getInputs() {
		return inputs;
	}

	@Override
	public double getEnergyCost() {
		return 10000;
	}

	// FIXME: canBeDone and useItems could use some improvements and
	// factorization. See AssemblyRecipe as well.

	@Override
	public boolean canBeDone(IInventory inv) {
		for (ItemStack requirement : inputs) {
			if (requirement == null) {
				continue;
			}

			int found = 0; // Amount of ingredient found in inventory
			int expected = requirement.stackSize;

			for (IInvSlot slot : InventoryIterator.getIterable(inv, ForgeDirection.UNKNOWN)) {
				ItemStack item = slot.getStackInSlot();
				if (item == null) {
					continue;
				}

				if (item.isItemEqual(requirement)) {
					found += item.stackSize; // Adds quantity of stack to
												// amount found
				}

				if (found >= expected) {
					break;
				}
			}

			// Return false if the amount of ingredient found
			// is not enough
			if (found < expected) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void useItems(IInventory inv) {
		ITransactor tran = Transactor.getTransactorFor(inv);

		for (ItemStack requirement : inputs) {
			for (int num = 0; num < requirement.stackSize; num++) {
				tran.remove(new ArrayStackFilter(requirement), ForgeDirection.UNKNOWN, true);
			}
		}
	}
}
