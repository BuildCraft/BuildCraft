/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.recipes;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.recipes.IAssemblyRecipeManager;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;

public class AssemblyRecipeManager implements IAssemblyRecipeManager {

	public static final AssemblyRecipeManager INSTANCE = new AssemblyRecipeManager();
	private List<AssemblyRecipe> assemblyRecipes = new LinkedList<AssemblyRecipe>();

	@Override
	public void addRecipe(double energyCost, ItemStack output, Object... input) {
		String name = Item.itemRegistry.getNameForObject(output.getItem());

		if (BuildCraftCore.recipesBlacklist.contains(name)) {
			return;
		}

		assemblyRecipes.add(new AssemblyRecipe(output, energyCost, input));
	}

	@Override
	public List<AssemblyRecipe> getRecipes() {
		return assemblyRecipes;
	}

	public static class AssemblyRecipe implements IAssemblyRecipe {

		public final ItemStack output;
		public final double energyCost;
		private final Object[] originalInput;
		private final Object[] processedInput;

		public AssemblyRecipe(ItemStack output, double energyCost, Object... inputs) {
			this.output = output.copy();
			this.energyCost = energyCost;
			this.originalInput = inputs;

			processedInput = new Object[inputs.length];
			for (int i = 0; i < inputs.length; i++) {
				if (inputs[i] instanceof String) {
					processedInput[i] = OreDictionary.getOres((String) inputs[i]);
				} else if (inputs[i] instanceof ItemStack) {
					processedInput[i] = inputs[i];
				} else if (inputs[i] instanceof Item) {
					processedInput[i] = new ItemStack((Item) inputs[i]);
				} else if (inputs[i] instanceof Block) {
					processedInput[i] = new ItemStack((Block) inputs[i], 1, OreDictionary.WILDCARD_VALUE);
				} else if (inputs[i] instanceof Integer) {
					processedInput[i] = inputs[i];
				} else {
					throw new IllegalArgumentException("Unknown Object passed to recipe!");
				}
			}
		}

		@Override
		public ItemStack getOutput() {
			return output.copy();
		}

		@Override
		public Object[] getInputs() {
			return originalInput;
		}

		@Override
		public double getEnergyCost() {
			return energyCost;
		}

		public boolean canBeDone(IInventory inv) {
			for (int i = 0; i < processedInput.length; i++) {
				if (processedInput[i] == null) {
					continue;
				}

				if (processedInput[i] instanceof ItemStack) {
					ItemStack requirement = (ItemStack) processedInput[i];
					int found = 0; // Amount of ingredient found in inventory
					int expected = requirement.stackSize;
					for (IInvSlot slot : InventoryIterator.getIterable(inv, ForgeDirection.UNKNOWN)) {
						ItemStack item = slot.getStackInSlot();
						if (item == null) {
							continue;
						}

						if (item.isItemEqual(requirement)) {
							found += item.stackSize; // Adds quantity of stack to amount found
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
				} else if (processedInput[i] instanceof List) {
					List<ItemStack> oreList = (List<ItemStack>) processedInput[i];
					int found = 0; // Amount of ingredient found in inventory
					// TODO: this i++ is highly dubious here, and against good
					// programming practises. Investigate and fix or document.
					//CHECKSTYLE.OFF: ModifiedControlVariable
					int expected = (Integer) processedInput[i++ + 1];
					//CHECKSTYLE.ON: ModifiedControlVariable
					for (IInvSlot slot : InventoryIterator.getIterable(inv, ForgeDirection.UNKNOWN)) {
						ItemStack item = slot.getStackInSlot();
						if (item == null) {
							continue;
						}
						for (ItemStack oreItem : oreList) {
							if (OreDictionary.itemMatches(oreItem, item, true)) {
								found += item.stackSize;
								break;
							}
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
			}

			return true;
		}

		public void useItems(IInventory inv) {
			ITransactor tran = Transactor.getTransactorFor(inv);
			Object[] input = processedInput;
			for (int i = 0; i < input.length; i++) {
				if (input[i] instanceof ItemStack) {
					ItemStack requirement = (ItemStack) input[i];
					for (int num = 0; num < requirement.stackSize; num++) {
						tran.remove(new ArrayStackFilter(requirement), ForgeDirection.UNKNOWN, true);
					}
				} else if (input[i] instanceof List) {
					List<ItemStack> oreList = (List<ItemStack>) input[i];
					int required = (Integer) input[i + 1];
					for (ItemStack ore : oreList) {
						for (int num = 0; num < required; num++) {
							if (tran.remove(new ArrayStackFilter(ore), ForgeDirection.UNKNOWN, true) != null) {
								required--;
							}
						}
						if (required <= 0) {
							break;
						}
					}
				}
			}
		}
	}
}
