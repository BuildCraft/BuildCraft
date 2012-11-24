/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.api.recipes;

import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraftforge.liquids.LiquidStack;



public class RefineryRecipe implements Comparable<RefineryRecipe> {

	private static SortedSet<RefineryRecipe> recipes = new TreeSet<RefineryRecipe>();
	
	public static void registerRefineryRecipe(RefineryRecipe recipe) {
		if (!recipes.contains(recipe)) {
			recipes.add(recipe);
		}
	}

	public static RefineryRecipe findRefineryRecipe(LiquidStack liquid1, LiquidStack liquid2) {
		for(RefineryRecipe recipe : recipes)
			if(recipe.matches(liquid1, liquid2))
				return recipe;
		
		return null;
	}
	
	/**
	 * Utility method which compares to only the types of source materials.
	 * We consider non-null < null in order that one-ingredient recipe is checked after
	 * the failure of matching two-ingredient recipes which include that liquid.
	 */
	private static int compareLiquids(LiquidStack liquid1, LiquidStack liquid2) {
		if (liquid1 == null) {
			if(liquid2 == null) {
				return 0;
			} else {
				return 1;
			}
		} else if(liquid2 == null) {
			return -1;
		} else if(liquid1.itemID != liquid2.itemID) {
			return liquid1.itemID - liquid2.itemID;
		} else if(liquid1.itemMeta == -1) {
			// liquid which meta is -1 has lower priority.
			if(liquid2.itemMeta == -1){
				return 0;
			}else{
				return 1;
			}
		} else {
			if(liquid2.itemMeta == -1) {
				return -1;
			} else {
				return liquid1.itemMeta - liquid2.itemMeta;
			}
		}
	}
	
	private static boolean isLiquidEqual(LiquidStack ingredient, LiquidStack liquid) {
		if(ingredient == null)
			return liquid == null;
		
		if(liquid == null)
			return false;
		
		// ignores meta matching if itemMeta == -1, as the same mannar as the vanilla recipes.
		if(ingredient.itemMeta == -1)
			return ingredient.itemID == liquid.itemID;
		
		return ingredient.isLiquidEqual(liquid);
	}
	

	public final LiquidStack ingredient1;
	public final LiquidStack ingredient2;
	public final LiquidStack result;
	
	public final int energy;
	public final int delay;

	public RefineryRecipe(int ingredientId1, int ingredientQty1, int ingredientId2, int ingredientQty2, int resultId, int resultQty,
			int energy, int delay) {
		this(new LiquidStack(ingredientId1, ingredientQty1, 0), new LiquidStack(ingredientId2, ingredientQty2, 0), new LiquidStack(resultId, resultQty, 0), energy, delay);
	}
	public RefineryRecipe(LiquidStack ingredient1, LiquidStack ingredient2, LiquidStack result, int energy, int delay) {
		
		// Sort starting materials.
		if(compareLiquids(ingredient1, ingredient2) > 0) {
			this.ingredient1 = ingredient2;
			this.ingredient2 = ingredient1;
		} else {
			this.ingredient1 = ingredient1;
			this.ingredient2 = ingredient2;
		}
		
		this.result = result;
		this.energy = energy;
		this.delay = delay;
	}

	public boolean matches(LiquidStack liquid1, LiquidStack liquid2) {
		
		// No inputs, return.
		if(liquid1 == null && liquid2 == null)
			return false;

		// Return if two ingredients are required but only one was supplied.
		if((ingredient1 != null && ingredient2 != null)
				&& (liquid1 == null || liquid2 == null))
			return false;
		
		if(ingredient1 != null) {
			
			if(ingredient2 == null)
				return isLiquidEqual(ingredient1, liquid1) || isLiquidEqual(ingredient1, liquid2);
			else
				return (isLiquidEqual(ingredient1, liquid1) && isLiquidEqual(ingredient2, liquid2))
						|| (isLiquidEqual(ingredient2, liquid1) && isLiquidEqual(ingredient1, liquid2));
			
		} else if(ingredient2 != null)
			return isLiquidEqual(ingredient2, liquid1) || isLiquidEqual(ingredient2, liquid2);
		else
			return false;

	}
	
	// Make instance of this class sortable.
	@Override
	public int compareTo(RefineryRecipe other) {
		
		int result;
		
		if(other == null) {
			return -1;
		}
		
		result = compareLiquids(ingredient1, other.ingredient1);
		if(result == 0) return compareLiquids(ingredient2, other.ingredient2);
		
		return result;
	}
	
	
	// equals() should be consistent with compareTo().
	@Override
	public boolean equals(Object obj) {
		if(obj != null && obj instanceof RefineryRecipe) {
			return this.compareTo((RefineryRecipe)obj) == 0;
		}
		return false;
	}
	
	// hashCode() should be overridden because equals() was overridden.
	@Override
	public int hashCode() {
		if(ingredient1 == null) {
			return 0;
		} else if(ingredient2 == null) {
			return ingredient1.itemID ^ ingredient1.itemMeta;
		}
		
		return ingredient1.itemID ^ ingredient1.itemMeta ^ ingredient2.itemID ^ ingredient2.itemMeta;
	}
}
