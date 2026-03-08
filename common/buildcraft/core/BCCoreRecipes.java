/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

// 1.18.2: use datagen
@Deprecated()
public class BCCoreRecipes {

//    public static void fmlPreInit() {
//        MinecraftForge.EVENT_BUS.register(BCCoreRecipes.class);
//    }

//    @SubscribeEvent
//    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
//        // TODO (1.13): define these in json
//
//        if (BCItems.Core.PAINTBRUSH != null) {
//            RecipeBuilderShaped builder = new RecipeBuilderShaped();
//            builder.add(" iw");
//            builder.add(" gi");
//            builder.add("s  ");
//            builder.map('i', Items.STRING);
//            builder.map('s', "stickWood");
//            builder.map('g', "gearWood");
//            builder.map('w', new ItemStack(Blocks.WOOL));
//            ItemStack cleanPaintbrush = new ItemStack(BCItems.Core.PAINTBRUSH);
//            builder.setResult(cleanPaintbrush);
//            builder.register();
//
//            for (EnumDyeColor colour : EnumDyeColor.values()) {
//                ItemPaintbrush_BC8.Brush brush = BCCoreItems.paintbrush.new Brush(colour);
//                ItemStack out = brush.save();
//
//                Object[] inputs = { //
//                    cleanPaintbrush, //
//                    ColourUtil.getDyeName(colour),//
//                };
//                ResourceLocation group = BCModules.CORE.createLocation("paintbrush_colouring");
//                ShapelessOreRecipe recipe = new ShapelessOreRecipe(group, out, inputs);
//                recipe.setRegistryName(BCModules.CORE.createLocation("paintbrush_" + colour.getName()));
//                event.getRegistry().register(recipe);
//            }
//        }
//
//        // if (BCItems.CORE_LIST != null) {
//        // if (BCBlocks.SILICON_TABLE_ASSEMBLY != null) {
//        // long mjCost = 2_000 * MjAPI.MJ;
//        // ImmutableSet<StackDefinition> required = ImmutableSet.of(//
//        // ArrayStackFilter.definition(8, Items.PAPER), //
//        // OreStackFilter.definition(ColourUtil.getDyeName(EnumDyeColor.GREEN)), //
//        // OreStackFilter.definition("dustRedstone")//
//        // );
//        // BuildcraftRecipeRegistry.assemblyRecipes
//        // .addRecipe(new AssemblyRecipe("list", mjCost, required, new ItemStack(BCItems.CORE_LIST)));
//        // } else {
//        // // handled in JSON
//        // }
//        // }
//
//        if (BCBlocks.Core.DECORATED != null) {
//            RecipeBuilderShaped builder = new RecipeBuilderShaped();
//            builder.add("sss");
//            builder.add("scs");
//            builder.add("sss");
//
//            // if (BCItems.Builders!= null) {
//            // builder.map('s', "stone");
//            // builder.map('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1, 2));
//            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.BLUEPRINT.ordinal()));
//            // builder.register();
//            //
//            // builder.map('c', new ItemStack(BCItems.Builders.SNAPSHOT, 1, 0));
//            // builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.TEMPLATE.ordinal()));
//            // builder.register();
//            // }
//
//            builder.map('s', Blocks.OBSIDIAN);
//            builder.map('c', Blocks.REDSTONE_BLOCK);
//            builder.setResult(new ItemStack(BCBlocks.Core.DECORATED, 16, EnumDecoratedBlock.LASER_BACK.ordinal()));
//            builder.register();
//        }
//    }
}
