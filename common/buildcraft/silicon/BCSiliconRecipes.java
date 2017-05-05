package buildcraft.silicon;

import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.inventory.filter.ArrayStackFilter;
import buildcraft.lib.inventory.filter.OreStackFilter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import buildcraft.api.BCBlocks;
import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IntegrationRecipe;

import buildcraft.lib.BCLib;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.RecipeBuilderShaped;
import net.minecraftforge.oredict.OreDictionary;

public class BCSiliconRecipes {
    public static void init() {
        if (BCBlocks.SILICON_LASER != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("rro");
            builder.add("rdo");
            builder.add("rro");
            builder.map('r', "dustRedstone");
            builder.map('o', Blocks.OBSIDIAN);
            builder.map('d', "gemDiamond");
            builder.setResult(new ItemStack(BCBlocks.SILICON_LASER));
            builder.register();
        }

        if (BCItems.SILICON_REDSTONE_CLIPSET != null) {
            addChipsetAssembly(1, null, EnumRedstoneChipset.RED);
            addChipsetAssembly(2, "ingotIron", EnumRedstoneChipset.IRON);
            addChipsetAssembly(4, "ingotGold", EnumRedstoneChipset.GOLD);
            addChipsetAssembly(6, "gemQuartz", EnumRedstoneChipset.QUARTZ);
            addChipsetAssembly(8, "gemDiamond", EnumRedstoneChipset.DIAMOND);
        }

        if(BCLib.DEV) {
            OreDictionary.registerOre("dyeYellow", Blocks.GOLD_BLOCK);
            OreDictionary.registerOre("dyeBlue", Blocks.LAPIS_BLOCK);
            OreDictionary.registerOre("dyeRed", Blocks.REDSTONE_BLOCK);

            IntegrationRecipeRegistry.INSTANCE.addRecipe(new IntegrationRecipe("potato-baker", 100,
                    ArrayStackFilter.definition(Items.POTATO),
                    ImmutableList.of(OreStackFilter.definition("dustRedstone")), new ItemStack(Items.BAKED_POTATO, 4)));
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
