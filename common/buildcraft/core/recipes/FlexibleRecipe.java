/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.recipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.recipes.IFlexibleRecipeIngredient;
import buildcraft.api.recipes.IFlexibleRecipeViewable;

import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.core.lib.inventory.filters.ArrayStackFilter;
import buildcraft.core.lib.inventory.filters.IStackFilter;

public class FlexibleRecipe<T> implements IFlexibleRecipe<T>, IFlexibleRecipeViewable {
	private class PreviewCrafter implements IFlexibleCrafter {
		private final SimpleInventory inventory;
		private final IFlexibleCrafter crafter;

		// TODO: Make a safe copy of fluids too
		public PreviewCrafter(IFlexibleCrafter crafter) {
			this.crafter = crafter;
			this.inventory = new SimpleInventory(crafter.getCraftingItemStackSize(), "Preview", 64);
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				ItemStack s = crafter.getCraftingItemStack(i);
				if (s != null) {
					inventory.setInventorySlotContents(i, s.copy());
				}
			}
		}

		@Override
		public int getCraftingItemStackSize() {
			return inventory.getSizeInventory();
		}

		@Override
		public ItemStack getCraftingItemStack(int slotid) {
			return inventory.getStackInSlot(slotid);
		}

		@Override
		public ItemStack decrCraftingItemStack(int slotid, int val) {
			return inventory.decrStackSize(slotid, val);
		}

		@Override
		public FluidStack getCraftingFluidStack(int tankid) {
			return crafter.getCraftingFluidStack(tankid);
		}

		@Override
		public FluidStack decrCraftingFluidStack(int tankid, int val) {
			return crafter.decrCraftingFluidStack(tankid, val);
		}

		@Override
		public int getCraftingFluidStackSize() {
			return crafter.getCraftingFluidStackSize();
		}
	}

	public int energyCost = 0;
	public long craftingTime = 0;
	public String id;

	public T output = null;

	public ArrayList<ItemStack> inputItems = new ArrayList<ItemStack>();
	public ArrayList<List<ItemStack>> inputItemsWithAlternatives = new ArrayList<List<ItemStack>>();

	public ArrayList<FluidStack> inputFluids = new ArrayList<FluidStack>();

	public FlexibleRecipe() {

	}

	public FlexibleRecipe(String id, T output, int iEnergyCost, long craftingTime, Object... input) {
		setContents(id, output, iEnergyCost, craftingTime, input);
	}

	public void setContents(String iid, Object iioutput, int iEnergyCost, long iCraftingTime, Object... input) {
		id = iid;

		Object ioutput = null;
		if (iioutput == null) {
			throw new IllegalArgumentException("The output of FlexibleRecipe " + iid + " is null! Rejecting recipe.");
		} else if (iioutput instanceof IFlexibleRecipeIngredient) {
			ioutput = ((IFlexibleRecipeIngredient) iioutput).getIngredient();
		} else {
			ioutput = iioutput;
		}

		if (ioutput instanceof ItemStack) {
			output = (T) ioutput;
		} else if (ioutput instanceof Item) {
			output = (T) new ItemStack((Item) ioutput);
		} else if (ioutput instanceof Block) {
			output = (T) new ItemStack((Block) ioutput);
		} else if (ioutput instanceof FluidStack) {
			output = (T) ioutput;
		} else {
			throw new IllegalArgumentException("An unknown object passed to recipe " + iid + " as output! (" + ioutput.getClass() + ")");
		}

		energyCost = iEnergyCost;
		craftingTime = iCraftingTime;

		for (int index = 0; index < input.length; index++) {
			Object i = null;
			Object ii = input[index];
			if (ii == null) {
				throw new IllegalArgumentException("An input of FlexibleRecipe " + iid + " is null! Rejecting recipe.");
			} else if (ii instanceof IFlexibleRecipeIngredient) {
				i = ((IFlexibleRecipeIngredient) ii).getIngredient();
			} else {
				i = ii;
			}

			if (i instanceof ItemStack) {
				inputItems.add((ItemStack) i);
			} else if (i instanceof Item) {
				inputItems.add(new ItemStack((Item) i));
			} else if (i instanceof Block) {
				inputItems.add(new ItemStack((Block) i));
			} else if (i instanceof FluidStack) {
				inputFluids.add((FluidStack) i);
			} else if (i instanceof List) {
				inputItemsWithAlternatives.add((List) i);
			} else if (i instanceof String) {
				if (index + 1 >= input.length) {
					inputItemsWithAlternatives.add(OreDictionary.getOres((String) i));
				} else if (input[index + 1] instanceof Integer) {
					index++;
					List<ItemStack> items = new ArrayList<ItemStack>();
					for (ItemStack stack : OreDictionary.getOres((String) i)) {
						stack.stackSize = (Integer) input[index];
						items.add(stack);
					}
					inputItemsWithAlternatives.add(items);
				} else {
					inputItemsWithAlternatives.add(OreDictionary.getOres((String) i));
				}
			} else {
				throw new IllegalArgumentException("An unknown object passed to recipe " + iid + " as input! (" + i.getClass() + ")");
			}
		}
	}


	@Override
	public boolean canBeCrafted(IFlexibleCrafter crafter) {
		return craft(crafter, true) != null;
	}

	@Override
	public CraftingResult<T> craft(IFlexibleCrafter baseCrafter, boolean preview) {
		if (output == null) {
			return null;
		}

		IFlexibleCrafter crafter = baseCrafter;
		if (preview) {
			crafter = new FakeFlexibleCrafter(baseCrafter);
		}

		CraftingResult<T> result = new CraftingResult<T>();

		result.recipe = this;
		result.energyCost = energyCost;
		result.craftingTime = craftingTime;

		for (ItemStack requirement : inputItems) {
			IStackFilter filter = new ArrayStackFilter(requirement);
			int amount = requirement.stackSize;

			if (consumeItems(crafter, result, filter, amount) != 0) {
				return null;
			}
		}

		// Item stacks with alternatives consumption

		for (List<ItemStack> requirements : inputItemsWithAlternatives) {
			IStackFilter filter = new ArrayStackFilter(requirements.toArray(new ItemStack[requirements.size()]));
			int amount = requirements.get(0).stackSize;

			if (consumeItems(crafter, result, filter, amount) != 0) {
				return null;
			}
		}

		// Fluid stacks consumption

		for (FluidStack requirement : inputFluids) {
			int amount = requirement.amount;

			for (int tankid = 0; tankid < crafter.getCraftingFluidStackSize(); tankid++) {
				FluidStack fluid = crafter.getCraftingFluidStack(tankid);

				if (fluid != null && fluid.isFluidEqual(requirement)) {
					int amountUsed;

					if (fluid.amount > amount) {
						amountUsed = amount;

						if (!preview) {
							crafter.decrCraftingFluidStack(tankid, amount);
						}

						amount = 0;
					} else {
						amountUsed = fluid.amount;

						if (!preview) {
							crafter.decrCraftingFluidStack(tankid, fluid.amount);
						}

						amount -= fluid.amount;
					}

					result.usedFluids.add(new FluidStack(requirement.getFluid(), amountUsed));
				}

				if (amount == 0) {
					break;
				}
			}

			if (amount != 0) {
				return null;
			}
		}

		// Output generation

		result.crafted = output;

		return result;
	}

	@Override
	public String getId() {
		return id;
	}

	private int consumeItems(IFlexibleCrafter crafter, CraftingResult<T> result, IStackFilter filter,
							 int amount) {
		int expected = amount;

		for (int slotid = 0; slotid < crafter.getCraftingItemStackSize(); ++slotid) {
			ItemStack stack = crafter.getCraftingItemStack(slotid);

			if (stack != null && filter.matches(stack)) {
				ItemStack removed;

				if (stack.stackSize >= expected) {
					removed = crafter.decrCraftingItemStack(slotid, expected);
					expected = 0;
				} else {
					removed = crafter.decrCraftingItemStack(slotid, stack.stackSize);
					expected -= removed.stackSize;
				}

				result.usedItems.add(removed);
			}

			if (expected == 0) {
				return 0;
			}
		}

		return amount;
	}

	@Override
	public CraftingResult<T> canCraft(ItemStack expectedOutput) {
		if (output instanceof ItemStack
				&& StackHelper.isMatchingItem(expectedOutput, (ItemStack) output)) {
			CraftingResult<T> result = new CraftingResult<T>();

			result.recipe = this;
			result.usedFluids = inputFluids;
			result.usedItems = inputItems;
			result.crafted = output;

			return result;
		} else {
			return null;
		}
	}

	@Override
	public Object getOutput() {
		return output;
	}

	@Override
	public Collection<Object> getInputs() {
		ArrayList<Object> inputs = new ArrayList<Object>();

		inputs.addAll(inputItems);
		inputs.addAll(inputItemsWithAlternatives);
		inputs.addAll(inputFluids);

		return inputs;
	}

	@Override
	public int getEnergyCost() {
		return energyCost;
	}

	@Override
	public long getCraftingTime() {
		return craftingTime;
	}
}
