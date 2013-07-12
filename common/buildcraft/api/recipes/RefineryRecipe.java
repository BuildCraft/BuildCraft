/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.recipes;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import net.minecraftforge.fluids.Fluid;

public class RefineryRecipe implements Comparable<RefineryRecipe> {

	private static SortedSet<RefineryRecipe> recipes = new TreeSet<RefineryRecipe>();

	public static void registerRefineryRecipe(RefineryRecipe recipe) {
		if (!recipes.contains(recipe)) {
			recipes.add(recipe);
		}
	}

	public static SortedSet<RefineryRecipe> getRecipes() {
		return Collections.unmodifiableSortedSet(recipes);
	}

	public static RefineryRecipe findRefineryRecipe(Fluid liquid1, Fluid liquid2) {
		for (RefineryRecipe recipe : recipes)
			if (recipe.matches(liquid1, liquid2))
				return recipe;

		return null;
	}

	public final Fluid ingredient1;
	public final Fluid ingredient2;
	public final Fluid result;

	public final int energy;
	public final int delay;

	public RefineryRecipe(Fluid ingredient1, Fluid ingredient2, Fluid result, int energy, int delay) {
		this.ingredient1 = ingredient1;
		this.ingredient2 = ingredient2;
		this.result = result;
		this.energy = energy;
		this.delay = delay;
	}

	public boolean matches(Fluid liquid1, Fluid liquid2) {

		// No inputs, return.
		if (liquid1 == null && liquid2 == null)
			return false;

		// Return if two ingredients are required but only one was supplied.
		if ((ingredient1 != null && ingredient2 != null) && (liquid1 == null || liquid2 == null))
			return false;

		if (ingredient1 != null) {
			if (ingredient2 == null)
				return ingredient1.getName().equals(liquid1) || ingredient1.getName().equals(liquid2);
			else
				return (ingredient1.getName().equals(liquid1) && ingredient2.getName().equals(liquid2))
						|| (ingredient2.getName().equals(liquid1) && ingredient1.getName().equals(liquid2));

		} else if (ingredient2 != null)
			return ingredient2.getName().equals(liquid1) || ingredient2.getName().equals(liquid2);
		else
			return false;

	}

	// Compares to only the types of source materials.
	// We consider non-null < null in order that one-ingredient recipe is checked after
	// the failure of matching two-ingredient recipes which include that liquid.
	@Override
	public int compareTo(RefineryRecipe other) {
	    return ComparisonChain.start()
	            .compare(ingredient1.getName(), other.ingredient1.getName())
	            .compare(ingredient2.getName(), other.ingredient2.getName())
	            .result();
	}

	// equals() should be consistent with compareTo().
	@Override
	public boolean equals(Object obj) {
		return obj instanceof RefineryRecipe &&
		        Objects.equal(ingredient1, ((RefineryRecipe)obj).ingredient1) &&
		        Objects.equal(ingredient1, ((RefineryRecipe)obj).ingredient2);
	}

	// hashCode() should be overridden because equals() was overridden.
	@Override
	public int hashCode() {
	    return Objects.hashCode(ingredient1, ingredient2);
	}
}
