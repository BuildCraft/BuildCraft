/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// 1.18.2: use datagen!
@Deprecated(forRemoval = true)
//@Mod.EventBusSubscriber(modid = BCTransport.MODID)
public class BCTransportRecipes {
//    @GameRegistry.ObjectHolder("buildcraftsilicon:assembly_table")
//    private static final Block SILICON_TABLE_ASSEMBLY = null;

    @SubscribeEvent
//    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    public static void registerRecipes(PlayerEvent.PlayerLoggedInEvent event) {
//        addPipeRecipe(BCTransportItems.pipeItemWood, "plankWood");
//        addPipeRecipe(BCTransportItems.pipeItemCobble, "cobblestone");
//        addPipeRecipe(BCTransportItems.pipeItemStone, "stone");
//        addPipeRecipe(BCTransportItems.pipeItemQuartz, "blockQuartz");
//        addPipeRecipe(BCTransportItems.pipeItemIron, "ingotIron");
//        addPipeRecipe(BCTransportItems.pipeItemGold, "ingotGold");
//        addPipeRecipe(BCTransportItems.pipeItemClay, Blocks.CLAY);
//        addPipeRecipe(BCTransportItems.pipeItemSandstone,
//                new ItemStack(Blocks.SANDSTONE, 1, OreDictionary.WILDCARD_VALUE));
//        addPipeRecipe(BCTransportItems.pipeItemVoid, new ItemStack(Items.DYE, 1, DyeColor.BLACK.getDyeDamage()),
//                "dustRedstone");
//        addPipeRecipe(BCTransportItems.pipeItemObsidian, Blocks.OBSIDIAN);
//        addPipeRecipe(BCTransportItems.pipeItemDiamond, Items.DIAMOND);
//        addPipeRecipe(BCTransportItems.pipeItemLapis, Blocks.LAPIS_BLOCK);
//        addPipeRecipe(BCTransportItems.pipeItemDaizuli, Blocks.LAPIS_BLOCK, Items.DIAMOND);
//        addPipeRecipe(BCTransportItems.pipeItemDiaWood, "plankWood", Items.DIAMOND);
//        addPipeRecipe(BCTransportItems.pipeItemStripes, "gearGold");
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeItemEmzuli, Blocks.LAPIS_BLOCK);
//
//        Item waterproof = BCTransportItems.waterproof;
//        if (waterproof == null) {
//            waterproof = Items.SLIME_BALL;
//        }
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipeFluidWood, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipeFluidCobble, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipeFluidStone, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipeFluidQuartz, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipeFluidIron, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipeFluidGold, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemClay, BCTransportItems.pipeFluidClay, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipeFluidSandstone, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemVoid, BCTransportItems.pipeFluidVoid, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemObsidian, BCTransportItems.pipeFluidObsidian, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipeFluidDiamond, waterproof);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemDiaWood, BCTransportItems.pipeFluidDiaWood, waterproof);
//
//        String upgrade = "dustRedstone";
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemWood, BCTransportItems.pipePowerWood, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemCobble, BCTransportItems.pipePowerCobble, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemStone, BCTransportItems.pipePowerStone, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemQuartz, BCTransportItems.pipePowerQuartz, upgrade);
//        // addPipeUpgradeRecipe(BCTransportItems.pipeItemIron, BCTransportItems.pipePowerIron, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemGold, BCTransportItems.pipePowerGold, upgrade);
//        addPipeUpgradeRecipe(BCTransportItems.pipeItemSandstone, BCTransportItems.pipePowerSandstone, upgrade);
//        // addPipeUpgradeRecipe(BCTransportItems.pipeItemDiamond, BCTransportItems.pipePowerDiamond, upgrade);

//        if (BCTransportItems.wire != null) {
//            for (DyeColor color : ColourUtil.COLOURS) {
////                String name = String.format("wire-%s", color.getUnlocalizedName());
//                String name = String.format("wire-%s", color.getSerializedName());
//                ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE),
////                        IngredientStack.of(ColourUtil.getDyeName(color)));
//                        IngredientStack.of(color.getTag()));
////                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 10_000 * MjAPI.MJ, input,
////                        new ItemStack(BCTransportItems.wire, 8, color.getMetadata())));
//                ItemStack wireStack = new ItemStack(BCTransportItems.wire.get(), 8);
//                ColourUtil.addColorTagToStack(wireStack, color);
//                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 10_000 * MjAPI.MJ, input,
//                        wireStack));
//            }
//        }
    }

//    private static void addPipeRecipe(ItemPipeHolder pipe, Object material) {
//        addPipeRecipe(pipe, material, material);
//    }

//    private static void addPipeRecipe(ItemPipeHolder pipe, Object left, Object right)
//    {
//        if (pipe == null) {
//            return;
//        }
//        ItemStack result = new ItemStack(pipe, 8);
//        IRecipe recipe = new ShapedOreRecipe(pipe.getRegistryName(), result, "lgr", 'l', left, 'r', right, 'g',
//                "blockGlassColorless");
//        recipe.setRegistryName(new ResourceLocation(pipe.getRegistryName() + "_colorless"));
//        ForgeRegistries.RECIPES.register(recipe);
//
//        for (DyeColor colour : DyeColor.values()) {
//            ItemStack resultStack = new ItemStack(pipe, 8, colour.getMetadata() + 1);
//            IRecipe colorRecipe = new ShapedOreRecipe(pipe.getRegistryName(), resultStack, "lgr", 'l', left, 'r', right,
//                    'g', "blockGlass" + ColourUtil.getName(colour));
//            colorRecipe.setRegistryName(new ResourceLocation(pipe.getRegistryName() + "_" + colour));
//            ForgeRegistries.RECIPES.register(colorRecipe);
//        }
//    }

//    private static void addPipeUpgradeRecipe(ItemPipeHolder from, ItemPipeHolder to, Object additional)
//    {
//        if (from == null || to == null) {
//            return;
//        }
//        if (additional == null) {
//            throw new NullPointerException("additional");
//        }
//
//        IRecipe returnRecipe = new ShapelessOreRecipe(to.getRegistryName(), new ItemStack(from), new ItemStack(to))
//                .setRegistryName(new ResourceLocation(to.getRegistryName() + "_undo"));
//        ForgeRegistries.RECIPES.register(returnRecipe);
//
//        NonNullList<Ingredient> list = NonNullList.create();
//        list.add(Ingredient.fromItem(from));
//        list.add(CraftingHelper.getIngredient(additional));
//
//        IRecipe upgradeRecipe = new ShapelessRecipes(to.getRegistryName().getResourcePath(), new ItemStack(to), list)
//                .setRegistryName(new ResourceLocation(to.getRegistryName() + "_colorless"));
//        ForgeRegistries.RECIPES.register(upgradeRecipe);
//
//        for (DyeColor colour : ColourUtil.COLOURS) {
//            ItemStack f = new ItemStack(from, 1, colour.getMetadata() + 1);
//            ItemStack t = new ItemStack(to, 1, colour.getMetadata() + 1);
//            IRecipe returnRecipeColored = new ShapelessOreRecipe(to.getRegistryName(), f, t)
//                    .setRegistryName(new ResourceLocation(to.getRegistryName() + "_" + colour.getName() + "_undo"));
//            ForgeRegistries.RECIPES.register(returnRecipeColored);
//
//            NonNullList<Ingredient> colorList = NonNullList.create();
//            colorList.add(Ingredient.fromStacks(f));
//            colorList.add(CraftingHelper.getIngredient(additional));
//
//            IRecipe upgradeRecipeColored = new ShapelessOreRecipe(to.getRegistryName(), colorList, t)
//                    .setRegistryName(new ResourceLocation(to.getRegistryName() + "_" + colour.getName()));
//            ForgeRegistries.RECIPES.register(upgradeRecipeColored);
//        }
//    }
}
