/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.inventory.filter.OreStackFilter;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import buildcraft.silicon.recipe.FacadeSwapRecipe;
import com.google.common.collect.ImmutableSet;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.mj.MjAPI;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;

import buildcraft.transport.item.ItemPipeHolder;

@Mod.EventBusSubscriber()
public class BCTransportRecipes {

    public static void init() {
        GameRegistry.addShapelessRecipe(new ItemStack(BCTransportItems.waterproof), new ItemStack(Items.DYE, 1, 2));
        GameRegistry.addShapelessRecipe(new ItemStack(BCTransportItems.waterproof), new ItemStack(Items.SLIME_BALL));

        if (BCTransportBlocks.filteredBuffer != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("wdw");
            builder.add("wcw");
            builder.add("wpw");
            builder.map('w', "plankWood");
            builder.map('p', Blocks.PISTON);
            builder.map('c', Blocks.CHEST);
            builder.map('d', BCTransportItems.pipeItemDiamond, Items.DIAMOND);
            builder.setResult(new ItemStack(BCTransportBlocks.filteredBuffer));
            builder.register();
        }

        if (BCTransportItems.pipeStructure != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("cgc");
            builder.map('c', "cobblestone");
            builder.map('g', Blocks.GRAVEL);
            builder.setResult(new ItemStack(BCTransportItems.pipeStructure, 8));
            builder.register();
        }

        addPipeRecipe(BCTransportItems.pipeItemWood, "plankWood");
        addPipeRecipe(BCTransportItems.pipeItemCobble, "cobblestone");
        addPipeRecipe(BCTransportItems.pipeItemStone, "stone");
        addPipeRecipe(BCTransportItems.pipeItemQuartz, "blockQuartz");
        addPipeRecipe(BCTransportItems.pipeItemIron, "ingotIron");
        addPipeRecipe(BCTransportItems.pipeItemGold, "ingotGold");
        addPipeRecipe(BCTransportItems.pipeItemClay, Blocks.CLAY);
        addPipeRecipe(BCTransportItems.pipeItemSandstone,
            new ItemStack(Blocks.SANDSTONE, 1, OreDictionary.WILDCARD_VALUE));
        addPipeRecipe(BCTransportItems.pipeItemVoid, new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()),
            "dustRedstone");
        addPipeRecipe(BCTransportItems.pipeItemObsidian, Blocks.OBSIDIAN);
        addPipeRecipe(BCTransportItems.pipeItemDiamond, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemLapis, Blocks.LAPIS_BLOCK);
        addPipeRecipe(BCTransportItems.pipeItemDaizuli, Blocks.LAPIS_BLOCK, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemDiaWood, "plankWood", Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemStripes, "gearGold");
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeItemEmzuli, Blocks.LAPIS_BLOCK);

        Item waterproof = BCTransportItems.waterproof;
        if (waterproof == null) {
            waterproof = Items.SLIME_BALL;
        }
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipeFluidWood, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipeFluidCobble, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipeFluidStone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipeFluidQuartz, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipeFluidIron, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipeFluidGold, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemClay, BCTransportItems.pipeFluidClay, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipeFluidSandstone, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemVoid, BCTransportItems.pipeFluidVoid, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemObsidian, BCTransportItems.pipeFluidObsidian, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipeFluidDiamond, waterproof);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeFluidDiaWood, waterproof);

        String upgrade = "dustRedstone";
        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipePowerWood, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipePowerCobble, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipePowerStone, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipePowerQuartz, upgrade);
        // addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipePowerIron, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipePowerGold, upgrade);
        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipePowerSandstone, upgrade);
        // addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipePowerDiamond, upgrade);

        if (BCTransportItems.wire != null) {
            for (EnumDyeColor color : ColourUtil.COLOURS) {
                String name = String.format("wire-%s", color.getUnlocalizedName());
                StackDefinition redstone = OreStackFilter.definition("dustRedstone");
                StackDefinition colorStack = OreStackFilter.definition(ColourUtil.getDyeName(color));
                ImmutableSet<StackDefinition> input = ImmutableSet.of(redstone, colorStack);
                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 10_000 * MjAPI.MJ, input,
                        new ItemStack(BCTransportItems.wire, 8, color.getMetadata())));
            }
        }

        GameRegistry.addShapelessRecipe(new ItemStack(BCTransportItems.plugBlocker), new ItemStack(BCTransportItems.pipeStructure, 1));


        if (BCTransportItems.plugPowerAdaptor != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("sis");
            builder.add("sgs");
            builder.add("srs");
            builder.map('s', BCTransportItems.pipeStructure);
            builder.map('g', "gearStone");
            builder.map('r', "dustRedstone");
            builder.map('i', "ingotGold");
            builder.setResult(new ItemStack(BCTransportItems.plugPowerAdaptor));
            builder.register();
        }
    }

    private static void addPipeRecipe(ItemPipeHolder pipe, Object material) {
        addPipeRecipe(pipe, material, material);
    }

    private static void addPipeRecipe(ItemPipeHolder pipe, Object left, Object right) {
        if (pipe == null) {
            return;
        }
        ItemStack result = new ItemStack(pipe, 8);
        IRecipe recipe = new ShapedOreRecipe(result, "lgr", 'l', left, 'r', right, 'g',
            "blockGlassColorless");
        GameRegistry.addRecipe(recipe);

        for (EnumDyeColor colour : EnumDyeColor.values()) {
            ItemStack resultStack = new ItemStack(pipe, 8, colour.getMetadata() + 1);
            IRecipe colorRecipe = new ShapedOreRecipe(resultStack, "lgr", 'l', left, 'r', right,
                'g', "blockGlass" + ColourUtil.getName(colour));
            GameRegistry.addRecipe(colorRecipe);
        }
    }

    private static void addPipeUpgradeRecipe(ItemPipeHolder from, ItemPipeHolder to, Object additional) {
        if (from == null || to == null) {
            return;
        }
        if (additional == null) {
            throw new NullPointerException("additional");
        }

        // TODO: Use RecipePipeColour instead!

        GameRegistry.addShapelessRecipe(new ItemStack(from), new ItemStack(to));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(to), new ItemStack(from), additional));

        for (EnumDyeColor colour : ColourUtil.COLOURS) {
            ItemStack f = new ItemStack(from, 1, colour.getMetadata() + 1);
            ItemStack t = new ItemStack(to, 1, colour.getMetadata() + 1);
            GameRegistry.addShapelessRecipe(f, t);
            GameRegistry.addRecipe(new ShapelessOreRecipe(t, f, additional));
        }
    }
}
