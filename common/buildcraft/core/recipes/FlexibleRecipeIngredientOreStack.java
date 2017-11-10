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
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.recipes.IFlexibleRecipeIngredient;

public class FlexibleRecipeIngredientOreStack implements IFlexibleRecipeIngredient {
    private final String oreName;
    private final int stackSize;

    public FlexibleRecipeIngredientOreStack(String oreName, int stackSize) {
        this.oreName = oreName;
        this.stackSize = stackSize;
    }

    @Override
    public Object getIngredient() {
        List<ItemStack> stacks = OreDictionary.getOres(oreName);
        List<ItemStack> result = new ArrayList<ItemStack>();

        if (stacks != null) {
            for (ItemStack stack : stacks) {
                ItemStack res = stack.copy();
                res.stackSize = stackSize;
                result.add(res);
            }
        }

        return result;
    }
}
