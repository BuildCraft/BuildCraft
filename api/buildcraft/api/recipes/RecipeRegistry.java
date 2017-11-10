/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 * Please check the contents of the license, which should be located
 * as "LICENSE.API" in the BuildCraft source code distribution.
 */
package buildcraft.api.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class RecipeRegistry {

	public static IRecipeManager<ItemStack> assemblyTable;
	public static IRecipeManager<ItemStack> integrationTable;
	public static IRecipeManager<FluidStack> refinery;
	public static IProgrammingRecipeManager programmingTable;

	private RecipeRegistry() {
	}
}
