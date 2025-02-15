package buildcraft.datagen.silicon;

import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.IngredientNBTBC;
import buildcraft.lib.recipe.assembly.AssemblyRecipeBuilder;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SiliconAssemblyRecipeGenerator extends RecipeProvider {
    private Consumer<FinishedRecipe> consumer;

    public SiliconAssemblyRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        this.consumer = consumer;

        ItemStack output = new ItemStack(BCSiliconItems.plugPulsar.get());

        ItemStack redstoneEngine;
        if (BCCoreBlocks.engineWood != null) {
            redstoneEngine = BCCoreBlocks.engineWood.get().getStack();
        } else {
            redstoneEngine = new ItemStack(Blocks.REDSTONE_BLOCK);
        }

        Set<IngredientStack> input = new HashSet<>();
//        input.add(new IngredientStack(Ingredient.fromStacks(redstoneEngine)));
        input.add(new IngredientStack(Ingredient.of(redstoneEngine)));
//        input.add(new IngredientStack(CraftingHelper.getIngredient("ingotIron"), 2));
        input.add(new IngredientStack(Ingredient.of(Tags.Items.INGOTS_IRON), 2));
//        AssemblyRecipe recipe = new AssemblyRecipeBasic("plug_pulsar", 1000 * MjAPI.MJ, input, output);
//        AssemblyRecipeRegistry.register(recipe);
        AssemblyRecipeBuilder.basic(1000 * MjAPI.MJ, input, output).save(consumer, "plug_pulsar");


//        IngredientStack lapis = IngredientStack.of("gemLapis");
        IngredientStack lapis = IngredientStack.of(Tags.Items.GEMS_LAPIS);
        makeGateAssembly(20_000, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.IRON);
        makeGateAssembly(40_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER,
                EnumRedstoneChipset.IRON, IngredientStack.of(new ItemStack(Blocks.NETHER_BRICKS)));
        makeGateAssembly(80_000, EnumGateMaterial.GOLD, EnumGateModifier.NO_MODIFIER, EnumRedstoneChipset.GOLD);

        makeGateModifierAssembly(40_000, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, lapis);
        makeGateModifierAssembly(60_000, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ,
                IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
        makeGateModifierAssembly(80_000, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND,
                IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));

        makeGateModifierAssembly(80_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.LAPIS, lapis);
        makeGateModifierAssembly(100_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.QUARTZ,
                IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
        makeGateModifierAssembly(120_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.DIAMOND,
                IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));

        makeGateModifierAssembly(100_000, EnumGateMaterial.GOLD, EnumGateModifier.LAPIS, lapis);
        makeGateModifierAssembly(140_000, EnumGateMaterial.GOLD, EnumGateModifier.QUARTZ,
                IngredientStack.of(EnumRedstoneChipset.QUARTZ.getStack()));
        makeGateModifierAssembly(180_000, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND,
                IngredientStack.of(EnumRedstoneChipset.DIAMOND.getStack()));

        AssemblyRecipeBuilder.basic(
                500 * MjAPI.MJ,
                ImmutableSet.of(IngredientStack.of(Blocks.DAYLIGHT_DETECTOR)),
                new ItemStack(BCSiliconItems.plugLightSensor.get())
        ).save(consumer, "light_sensor");

//        AssemblyRecipeRegistry.register(FacadeAssemblyRecipes.INSTANCE);
        AssemblyRecipeBuilder.facade().save(consumer, "facade");


        for (DyeColor colour : ColourUtil.COLOURS) {
            String name = String.format("lens_regular_%s", colour.getName());
//            IngredientStack stainedGlass = IngredientStack.of("blockGlass" + ColourUtil.getName(colour));
            IngredientStack stainedGlass = IngredientStack.of(TagKey.create(Registries.ITEM, new ResourceLocation("forge:glass/" + colour.getName())));
            ImmutableSet<IngredientStack> input1 = ImmutableSet.of(stainedGlass);
            ItemStack output1 = BCSiliconItems.plugLens.get().getStack(colour, false);
//            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));
            AssemblyRecipeBuilder.basic(500 * MjAPI.MJ, input1, output1).save(consumer, name);

            name = String.format("lens_filter_%s", colour.getName());
            output1 = BCSiliconItems.plugLens.get().getStack(colour, true);
            input1 = ImmutableSet.of(stainedGlass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
//            AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));
            AssemblyRecipeBuilder.basic(500 * MjAPI.MJ, input1, output1).save(consumer, name);
        }

//        IngredientStack glass = IngredientStack.of("blockGlass");
        IngredientStack glass = IngredientStack.of(Tags.Items.GLASS_COLORLESS);
        ImmutableSet<IngredientStack> input2 = ImmutableSet.of(glass);
        ItemStack output2 = BCSiliconItems.plugLens.get().getStack(null, false);
//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-regular", 500 * MjAPI.MJ, input, output));
        AssemblyRecipeBuilder.basic(500 * MjAPI.MJ, input2, output2).save(consumer, "lens_regular");

        output2 = BCSiliconItems.plugLens.get().getStack(null, true);
        input2 = ImmutableSet.of(glass, IngredientStack.of(new ItemStack(Blocks.IRON_BARS)));
//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("lens-filter", 500 * MjAPI.MJ, input, output));
        AssemblyRecipeBuilder.basic(500 * MjAPI.MJ, input2, output2).save(consumer, "lens_filter");


//        ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of("dustRedstone"));
        ImmutableSet<IngredientStack> input3 = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE));
        ItemStack output3 = EnumRedstoneChipset.RED.getStack(1);
//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("redstone_chipset", 10000 * MjAPI.MJ, input, output));
        AssemblyRecipeBuilder.basic(10000 * MjAPI.MJ, input3, output3).save(consumer, "redstone_chipset");

//        input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("ingotIron"));
        input3 = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.INGOTS_IRON));
        output3 = EnumRedstoneChipset.IRON.getStack(1);
//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("iron_chipset", 20000 * MjAPI.MJ, input, output));
        AssemblyRecipeBuilder.basic(40000 * MjAPI.MJ, input3, output3).save(consumer, "iron_chipset");

//        input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("ingotGold"));
        input3 = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.INGOTS_GOLD));
        output3 = EnumRedstoneChipset.GOLD.getStack(1);
//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gold_chipset", 40000 * MjAPI.MJ, input, output));
        AssemblyRecipeBuilder.basic(20000 * MjAPI.MJ, input3, output3).save(consumer, "gold_chipset");

//        input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("gemQuartz"));
        input3 = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.GEMS_QUARTZ));
        output3 = EnumRedstoneChipset.QUARTZ.getStack(1);
//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("quartz_chipset", 60000 * MjAPI.MJ, input, output));
        AssemblyRecipeBuilder.basic(60000 * MjAPI.MJ, input3, output3).save(consumer, "quartz_chipset");

//        input = ImmutableSet.of(IngredientStack.of("dustRedstone"), IngredientStack.of("gemDiamond"));
        input3 = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(Tags.Items.GEMS_DIAMOND));
        output3 = EnumRedstoneChipset.DIAMOND.getStack(1);
//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("diamond_chipset", 80000 * MjAPI.MJ, input, output));
        AssemblyRecipeBuilder.basic(80000 * MjAPI.MJ, input3, output3).save(consumer, "diamond_chipset");


        ImmutableSet.Builder<IngredientStack> input4 = ImmutableSet.builder();
        if (BCCoreItems.wrench != null) {
            input4.add(IngredientStack.of(BCCoreItems.wrench.get()));
        } else {
            input4.add(IngredientStack.of(Items.STICK));
            input4.add(IngredientStack.of(Items.IRON_INGOT));
        }

        if (BCSiliconItems.chipsetRedstone != null) {
            input4.add(IngredientStack.of(EnumRedstoneChipset.IRON.getStack(1)));
        } else {
//            input.add(IngredientStack.of("dustRedstone"));
            input4.add(IngredientStack.of(Tags.Items.DUSTS_REDSTONE));
//            input.add(IngredientStack.of("dustRedstone"));
            input4.add(IngredientStack.of(Tags.Items.DUSTS_REDSTONE));
//            input.add(IngredientStack.of("ingotGold"));
            input4.add(IngredientStack.of(Tags.Items.INGOTS_GOLD));
        }

//        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("gate_copier", 500 * MjAPI.MJ, input.build(), new ItemStack(BCSiliconItems.gateCopier.get())));
        AssemblyRecipeBuilder.basic(500 * MjAPI.MJ, input4.build(), new ItemStack(BCSiliconItems.gateCopier.get())).save(consumer, "gate_copier");
    }

    private void makeGateModifierAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier, IngredientStack... mods) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
//            String name = String.format("gate-modifier-%s-%s-%s", logic, material, modifier);
            String name = String.format("gate_modifier_%s_%s_%s", logic.tag, material.tag, modifier.tag);
            GateVariant variantFrom = new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER);
//            ItemStack toUpgrade = BCSiliconItems.plugGate.get().getStack(variantFrom);
            ItemStack toUpgrade = ItemPluggableGate.getStack(variantFrom);
//            ItemStack output = BCSiliconItems.plugGate.get().getStack(new GateVariant(logic, material, modifier));
            ItemStack output = ItemPluggableGate.getStack(new GateVariant(logic, material, modifier));
            ImmutableSet.Builder<IngredientStack> inputBuilder = new ImmutableSet.Builder<>();
            inputBuilder.add(new IngredientStack(new IngredientNBTBC(toUpgrade)));
            inputBuilder.add(mods);
            ImmutableSet<IngredientStack> input = inputBuilder.build();
//            AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
            AssemblyRecipeBuilder.basic(MjAPI.MJ * multiplier, input, output).save(consumer, name);
        }
    }

    private void makeGateAssembly(int multiplier, EnumGateMaterial material, EnumGateModifier modifier, EnumRedstoneChipset chipset, IngredientStack... additional) {
        ImmutableSet.Builder<IngredientStack> temp = ImmutableSet.builder();
        temp.add(new IngredientStack(new IngredientNBTBC(chipset.getStack())));
        temp.add(additional);
        ImmutableSet<IngredientStack> input = temp.build();

//        String name = String.format("gate-and-%s-%s", material, modifier);
        String name = String.format("gate_and_%s_%s", material.tag, modifier.tag);
//        ItemStack output = BCSiliconItems.variantGateMap.get(new GateVariant(EnumGateLogic.AND, material, modifier));
        ItemStack output = ItemPluggableGate.getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
//        AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
        AssemblyRecipeBuilder.basic(MjAPI.MJ * multiplier, input, output).save(consumer, name);

//        name = String.format("gate-or-%s-%s", material, modifier);
        name = String.format("gate_or_%s_%s", material.tag, modifier.tag);
//        output = BCSiliconItems.variantGateMap.get(new GateVariant(EnumGateLogic.OR, material, modifier)).get().;
        output = ItemPluggableGate.getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
//        AssemblyRecipeRegistry.register((new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output)));
        AssemblyRecipeBuilder.basic(MjAPI.MJ * multiplier, input, output).save(consumer, name);
    }

    @Override
    public String getName() {
        return "BuildCraft Silicon Assembly Recipe Generator";
    }
}
