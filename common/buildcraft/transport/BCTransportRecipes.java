package buildcraft.transport;

import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.inventory.filter.OreStackFilter;
import com.google.common.collect.ImmutableSet;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;

import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.NBTAwareShapedOreRecipe;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import buildcraft.transport.gate.EnumGateLogic;
import buildcraft.transport.gate.EnumGateMaterial;
import buildcraft.transport.gate.EnumGateModifier;
import buildcraft.transport.gate.GateVariant;
import buildcraft.transport.item.ItemPipeHolder;
import buildcraft.transport.recipe.FacadeAssemblyRecipes;
import buildcraft.transport.recipe.FacadeSwapRecipe;

public class BCTransportRecipes {
    public static void init() {
        if (BCTransportItems.waterproof != null) {
            GameRegistry.addShapelessRecipe(new ItemStack(BCTransportItems.waterproof), new ItemStack(Items.DYE, 1, 2));
        }

        if (BCTransportBlocks.filteredBuffer != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("wdw");
            builder.add("wcw");
            builder.add("wpw");
            builder.map('w', "plankWood");
            builder.map('p', Blocks.PISTON);
            builder.map('c', Blocks.CHEST);
            builder.map('d', BCItems.TRANSPORT_PIPE_DIAMOND_ITEM, Items.DIAMOND);
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
        addPipeRecipe(BCTransportItems.pipeItemSandstone, new ItemStack(Blocks.SANDSTONE, 1, OreDictionary.WILDCARD_VALUE));
        addPipeRecipe(BCTransportItems.pipeItemVoid, new ItemStack(Items.DYE, 1, EnumDyeColor.BLACK.getDyeDamage()), "dustRedstone");
        addPipeRecipe(BCTransportItems.pipeItemObsidian, Blocks.OBSIDIAN);
        addPipeRecipe(BCTransportItems.pipeItemDiamond, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemLapis, Blocks.LAPIS_BLOCK);
        addPipeRecipe(BCTransportItems.pipeItemDaizuli, Blocks.LAPIS_BLOCK, Items.DIAMOND);
        addPipeRecipe(BCTransportItems.pipeItemDiaWood, "plankWood", Items.DIAMOND);

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

        if (BCTransportItems.plugBlocker != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("s");
            builder.map('s', BCTransportItems.pipeStructure);
            builder.setResult(new ItemStack(BCTransportItems.plugBlocker, 4));
            builder.register();
        }

        if (BCTransportItems.plugPulsar != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("rer");
            builder.add("gpg");
            if (BCCoreBlocks.engine != null && BCCoreBlocks.engine.isRegistered(EnumEngineType.WOOD)) {
                builder.map('e', BCCoreBlocks.engine.getStack(EnumEngineType.WOOD));
            } else {
                builder.map('e', Blocks.REDSTONE_BLOCK);
            }
            builder.map('p', BCTransportItems.plugBlocker, Blocks.COBBLESTONE);
            builder.map('g', "gearIron");
            builder.map('r', "dustRedstone");
            builder.setResult(new ItemStack(BCTransportItems.plugPulsar));
            builder.register();
        }

        if (BCTransportItems.plugGate != null) {
            // You can craft some of the basic gate types in a normal crafting table
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mrm");
            builder.add(" b ");
            builder.map('r', "dustRedstone");
            builder.map('b', BCTransportItems.plugBlocker, Blocks.COBBLESTONE);

            // Base craftable types

            builder.map('m', Items.BRICK);
            makeGateRecipe(builder, EnumGateMaterial.CLAY_BRICK, EnumGateModifier.NO_MODIFIER);

            builder.map('m', "ingotIron");
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);

            builder.map('m', Items.NETHERBRICK);
            makeGateRecipe(builder, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER);

            // Iron modifier addition
            GateVariant variant = new GateVariant(EnumGateLogic.AND, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER);
            ItemStack ironGateBase = BCTransportItems.plugGate.getStack(variant);
            builder = new RecipeBuilderShaped();
            builder.add(" m ");
            builder.add("mgm");
            builder.add(" m ");
            builder.map('g', ironGateBase);

            builder.map('m', new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.LAPIS);

            builder.map('m', Items.QUARTZ);
            makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ);

            if (BCCoreItems.diamondShard != null) {
                builder.map('m', BCCoreItems.diamondShard);
                makeGateRecipe(builder, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND);
            }

            // And Gate <-> Or Gate (shapeless)
            for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
                if (material == EnumGateMaterial.CLAY_BRICK) {
                    continue;
                }
                for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                    GateVariant varAnd = new GateVariant(EnumGateLogic.AND, material, modifier);
                    ItemStack resultAnd = BCTransportItems.plugGate.getStack(varAnd);

                    GateVariant varOr = new GateVariant(EnumGateLogic.OR, material, modifier);
                    ItemStack resultOr = BCTransportItems.plugGate.getStack(varOr);

                    GameRegistry.addRecipe(new NBTAwareShapedOreRecipe(resultAnd, "i", 'i', resultOr));
                    GameRegistry.addRecipe(new NBTAwareShapedOreRecipe(resultOr, "i", 'i', resultAnd));
                }
            }
            StackDefinition lapis = OreStackFilter.definition("gemLapis");
            makeGateAssembly(20_000, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.IRON);
            makeGateAssembly(40_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.IRON, ArrayStackFilter.definition(new ItemStack(Blocks.NETHER_BRICK)));
            makeGateAssembly(80_000, EnumGateMaterial.GOLD, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.GOLD);

            makeGateModifierAssembly(40_000, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(60_000, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ, ArrayStackFilter.definition(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(80_000, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND, ArrayStackFilter.definition(EnumRedstoneChipset.DIAMOND.getStack()));

            makeGateModifierAssembly(80_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(100_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.QUARTZ, ArrayStackFilter.definition(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(120_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.DIAMOND, ArrayStackFilter.definition(EnumRedstoneChipset.DIAMOND.getStack()));

            makeGateModifierAssembly(100_000, EnumGateMaterial.GOLD, EnumGateModifier.LAPIS, lapis);
            makeGateModifierAssembly(140_000, EnumGateMaterial.GOLD, EnumGateModifier.QUARTZ, ArrayStackFilter.definition(EnumRedstoneChipset.QUARTZ.getStack()));
            makeGateModifierAssembly(180_000, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND, ArrayStackFilter.definition(EnumRedstoneChipset.DIAMOND.getStack()));
        }

        if (BCTransportItems.wire != null) {
            for (EnumDyeColor color : ColourUtil.COLOURS) {
                String name = String.format("wire-%s", color.getUnlocalizedName());
                ImmutableSet<StackDefinition> input = ImmutableSet.of(OreStackFilter.definition("dustRedstone"), OreStackFilter.definition(ColourUtil.getDyeName(color)));
                AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, 10_000 * MjAPI.MJ, input, new ItemStack(BCTransportItems.wire, 8, color.getMetadata())));
            }
        }

        if (BCTransportItems.plugLens != null) {
            for (EnumDyeColor colour : ColourUtil.COLOURS) {
                String name = String.format("lens-regular-%s", colour.getUnlocalizedName());
                StackDefinition stainedGlass = OreStackFilter.definition("blockGlass" + ColourUtil.getName(colour));
                ImmutableSet<StackDefinition> input = ImmutableSet.of(stainedGlass);
                ItemStack output = BCTransportItems.plugLens.getStack(colour, false);
                AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, 500 * MjAPI.MJ, input, output));

                name = String.format("lens-filter-%s", colour.getUnlocalizedName());
                output = BCTransportItems.plugLens.getStack(colour, true);
                input = ImmutableSet.of(stainedGlass, ArrayStackFilter.definition(new ItemStack(Blocks.IRON_BARS)));
                AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, 500 * MjAPI.MJ, input, output));
            }

            StackDefinition glass = OreStackFilter.definition("blockGlass");
            ImmutableSet<StackDefinition> input = ImmutableSet.of(glass);
            ItemStack output = BCTransportItems.plugLens.getStack(null, false);
            AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe("lens-regular", 500 * MjAPI.MJ, input, output));

            output = BCTransportItems.plugLens.getStack(null, true);
            input = ImmutableSet.of(glass, ArrayStackFilter.definition(new ItemStack(Blocks.IRON_BARS)));
            AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe("lens-filter", 500 * MjAPI.MJ, input, output));
        }

        if (BCTransportItems.plugFacade != null) {
            AssemblyRecipeRegistry.INSTANCE.addRecipeProvider(FacadeAssemblyRecipes.INSTANCE);
            GameRegistry.addRecipe(FacadeSwapRecipe.INSTANCE);
        }
    }

    private static void makeGateModifierAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier, StackDefinition... mods) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            String name = String.format("gate-modifier-%s-%s-%s", logic, material, modifier);
            ItemStack toUpgrade = BCTransportItems.plugGate.getStack(new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER));
            ItemStack output = BCTransportItems.plugGate.getStack(new GateVariant(logic, material, modifier));
            ImmutableSet<StackDefinition> input = new ImmutableSet.Builder<StackDefinition>().add(ArrayStackFilter.definition(toUpgrade)).add(mods).build();
            AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, MjAPI.MJ * multiplier, input, output));
        }
    }

    private static void makeGateAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier, EnumRedstoneChipset chipset, StackDefinition... additional) {
        ImmutableSet.Builder<StackDefinition> temp = ImmutableSet.builder();
        temp.add(ArrayStackFilter.definition(chipset.getStack()));
        temp.add(additional);
        ImmutableSet<StackDefinition> input = temp.build();

        String name = String.format("gate-and-%s-%s", material, modifier);
        ItemStack output = BCTransportItems.plugGate.getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, MjAPI.MJ * multiplier, input, output));

        name = String.format("gate-or-%s-%s", material, modifier);
        output = BCTransportItems.plugGate.getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(name, MjAPI.MJ * multiplier, input, output));
    }

    private static void makeGateRecipe(RecipeBuilderShaped builder, EnumGateMaterial material, EnumGateModifier modifier) {
        GateVariant variant = new GateVariant(EnumGateLogic.AND, material, modifier);
        builder.setResult(BCTransportItems.plugGate.getStack(variant));
        builder.registerNbtAware();
    }

    private static void addPipeRecipe(ItemPipeHolder pipe, Object material) {
        addPipeRecipe(pipe, material, material);
    }

    private static void addPipeRecipe(ItemPipeHolder pipe, Object left, Object right) {
        if (pipe == null) {
            return;
        }

        // TODO: Use RecipePipeColour instead!
        RecipeBuilderShaped pipeBuilderSingle = new RecipeBuilderShaped();
        pipeBuilderSingle.add("lgr");

        pipeBuilderSingle.map('l', left);
        pipeBuilderSingle.map('r', right);
        pipeBuilderSingle.map('g', "blockGlassColorless");
        pipeBuilderSingle.setResult(new ItemStack(pipe, 8, 0));
        pipeBuilderSingle.register();

        for (EnumDyeColor colour : EnumDyeColor.values()) {
            pipeBuilderSingle.map('g', "blockGlass" + ColourUtil.getName(colour));
            pipeBuilderSingle.setResult(new ItemStack(pipe, 8, colour.getMetadata() + 1));
            pipeBuilderSingle.register();
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
