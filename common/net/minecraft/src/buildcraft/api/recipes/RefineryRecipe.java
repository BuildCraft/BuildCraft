/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.api.recipes;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.api.liquids.LiquidStack;

public class RefineryRecipe {

	private static LinkedList<RefineryRecipe> recipes = new LinkedList<RefineryRecipe>();
	
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
		this.ingredient1 = ingredient1;
		this.ingredient2 = ingredient2;
		this.result = result;
		this.energy = energy;
		this.delay = delay;
	}

	public boolean matches(LiquidStack liquid1, LiquidStack liquid2) {
		if(liquid1 == null && liquid2 == null)
			return false;
		
		if((ingredient1 != null && ingredient2 != null)
				&& (liquid1 == null || liquid2 == null))
			return false;
		
		if(liquid1 != null) {
			
			if(liquid2 == null || liquid2.itemID <= 0) {
				return liquid1.isLiquidEqual(ingredient1) || liquid1.isLiquidEqual(ingredient2);
			} else {
				return (liquid1.isLiquidEqual(ingredient1) && liquid2.isLiquidEqual(ingredient2))
						|| (liquid2.isLiquidEqual(ingredient1) && liquid1.isLiquidEqual(ingredient2));
			}
			
		} else			
			return liquid2.isLiquidEqual(ingredient1) || liquid2.isLiquidEqual(ingredient2);
		
	}
}
