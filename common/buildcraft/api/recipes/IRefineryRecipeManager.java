/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.recipes;

import java.util.SortedSet;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public interface IRefineryRecipeManager {

	void addRecipe(FluidStack ingredient, FluidStack result, int energy, int delay);

	void addRecipe(FluidStack ingredient1, FluidStack ingredient2, FluidStack result, int energy, int delay);

	SortedSet<? extends IRefineryRecipe> getRecipes();

	IRefineryRecipe findRefineryRecipe(FluidStack ingredient1, FluidStack ingredient2);

	public static interface IRefineryRecipe {

		FluidStack getIngredient1();

		FluidStack getIngredient2();

		FluidStack getResult();

		int getEnergyCost();

		int getTimeRequired();
	}
}
