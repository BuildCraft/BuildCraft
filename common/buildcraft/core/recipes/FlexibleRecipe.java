/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.core.inventory.FluidHandlerCopy;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryCopy;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.core.inventory.filters.IStackFilter;

public class FlexibleRecipe implements IFlexibleRecipe {
	public double energyCost = 0;
	public String id;

	public ItemStack outputItems = null;
	public FluidStack outputFluids = null;

	public ArrayList<ItemStack> inputItems = new ArrayList<ItemStack>();
	public ArrayList<List<ItemStack>> inputItemsWithAlternatives = new ArrayList<List<ItemStack>>();

	public ArrayList<FluidStack> inputFluids = new ArrayList<FluidStack>();

	public FlexibleRecipe() {

	}

	public FlexibleRecipe(String id, Object output, double iEnergyCost, Object... input) {
		setContents(id, output, iEnergyCost, input);
	}

	public void setContents(String iid, Object output, double iEnergyCost, Object... input) {
		id = iid;

		if (output instanceof ItemStack) {
			outputItems = (ItemStack) output;
		} else if (output instanceof Item) {
			outputItems = new ItemStack((Item) output);
		} else if (output instanceof Block) {
			outputItems = new ItemStack((Block) output);
		} else if (output instanceof FluidStack) {
			outputFluids = (FluidStack) output;
		} else {
			throw new IllegalArgumentException("Unknown Object passed to recipe!");
		}

		energyCost = iEnergyCost;

		for (Object i : input) {
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
			} else {
				throw new IllegalArgumentException("Unknown Object passed to recipe!");
			}
		}
	}


	@Override
	public boolean canBeCrafted(IInventory items, IFluidHandler fluids) {
		return craftPreview(items, fluids) != null;
	}

	@Override
	public final CraftingResult craftPreview(IInventory items, IFluidHandler fluids) {
		return craft(items == null ? null : new InventoryCopy(items),
				fluids == null ? null : new FluidHandlerCopy(fluids));
	}

	@Override
	public CraftingResult craft(IInventory items, IFluidHandler fluids) {
		CraftingResult result = new CraftingResult();

		result.recipe = this;
		result.energyCost = energyCost;

		// Item simple stacks consumption

		if (items == null && inputItems.size() > 0) {
			return null;
		}

		if (items != null) {
			ITransactor tran = Transactor.getTransactorFor(items);

			for (ItemStack requirement : inputItems) {
				IStackFilter filter = new ArrayStackFilter(requirement);

				for (int num = 0; num < requirement.stackSize; num++) {
					ItemStack s = tran.remove(filter, ForgeDirection.UNKNOWN, true);

					if (s == null) {
						return null;
					} else {
						result.usedItems.add(s);
					}
				}
			}
		}

		// Item stacks with alternatives consumption

		if (items == null && inputItemsWithAlternatives.size() > 0) {
			return null;
		}

		if (items != null) {
			ITransactor tran = Transactor.getTransactorFor(items);

			for (List<ItemStack> requirements : inputItemsWithAlternatives) {

				int required = requirements.get(0).stackSize;

				IStackFilter filter = new ArrayStackFilter(requirements.toArray(new ItemStack [0]));

				for (int num = 0; num < required; num++) {
					ItemStack s = tran.remove(filter, ForgeDirection.UNKNOWN, true);

					if (s != null) {
						result.usedItems.add(s);

						required--;

						if (required == 0) {
							break;
						}
					}
				}

				if (required > 0) {
					return null;
				}
			}
		}

		// Fluid stacks consumption

		if (fluids == null && inputFluids.size() > 0) {
			return null;
		}

		if (fluids != null) {
			for (FluidStack requirement : inputFluids) {
				for (FluidTankInfo info : fluids.getTankInfo(ForgeDirection.UNKNOWN)) {
					if (info.fluid.isFluidEqual(requirement)) {
						int amountUsed = 0;

						if (info.fluid.amount > requirement.amount) {
							requirement.amount = 0;
							info.fluid.amount -= requirement.amount;
							amountUsed += requirement.amount;
						} else {
							requirement.amount -= info.fluid.amount;
							info.fluid.amount = 0;
							amountUsed += info.fluid.amount;
						}

						result.usedFluids.add(new FluidStack(requirement.fluidID, amountUsed));
					}
				}
			}
		}

		for (FluidStack requirement : inputFluids) {
			if (requirement.amount > 0) {
				return null;
			}
		}

		// Output generation

		if (outputItems != null) {
			result.crafted = outputItems;

			return result;
		} else if (outputFluids != null) {
			result.crafted = outputFluids;

			return result;
		} else {
			return null;
		}
	}

	@Override
	public String getId() {
		return id;
	}
}
