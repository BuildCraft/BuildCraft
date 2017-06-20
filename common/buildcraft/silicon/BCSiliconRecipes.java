/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.api.recipes.StackDefinition;

import buildcraft.lib.BCLib;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.inventory.filter.OreStackFilter;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;

public class BCSiliconRecipes {
    public static void init() {
        if (BCItems.SILICON_REDSTONE_CLIPSET != null) {
            addChipsetAssembly(1, null, EnumRedstoneChipset.RED);
            addChipsetAssembly(2, "ingotIron", EnumRedstoneChipset.IRON);
            addChipsetAssembly(4, "ingotGold", EnumRedstoneChipset.GOLD);
            addChipsetAssembly(6, "gemQuartz", EnumRedstoneChipset.QUARTZ);
            addChipsetAssembly(8, "gemDiamond", EnumRedstoneChipset.DIAMOND);
        }

        if (BCLib.DEV) {
            OreDictionary.registerOre("dyeYellow", Blocks.GOLD_BLOCK);
            OreDictionary.registerOre("dyeBlue", Blocks.LAPIS_BLOCK);
            OreDictionary.registerOre("dyeRed", Blocks.REDSTONE_BLOCK);

            StackDefinition target = ArrayStackFilter.definition(Items.POTATO);
            ImmutableList<StackDefinition> required = ImmutableList.of(OreStackFilter.definition("dustRedstone"));
            ItemStack output = new ItemStack(Items.BAKED_POTATO, 4);
            IntegrationRecipeRegistry.INSTANCE.addRecipe(new IntegrationRecipe("potato-baker", 100 * MjAPI.MJ, target, required, output));
        }
    }

    private static void addChipsetAssembly(int multiplier, String additional, EnumRedstoneChipset type) {
        ItemStack output = type.getStack();
        ImmutableSet.Builder<StackDefinition> inputs = ImmutableSet.builder();
        inputs.add(OreStackFilter.definition("dustRedstone"));
        if (additional != null) {
            inputs.add(OreStackFilter.definition(additional));
        }

        String name = String.format("chipset-%s", type);
        AssemblyRecipe recp = new AssemblyRecipe(name, multiplier * 10_000 * MjAPI.MJ, inputs.build(), output);
        AssemblyRecipeRegistry.INSTANCE.addRecipe(recp);
    }
}
