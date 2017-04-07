package buildcraft.silicon;

import com.google.common.collect.ImmutableSet;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.BCBlocks;
import buildcraft.api.BCItems;
import buildcraft.api.enums.EnumRedstoneChipset;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;

import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.recipe.RecipeBuilderShaped;

public class BCSiliconRecipes {
    public static void init() {
        if (BCBlocks.SILICON_LASER != null) {
            RecipeBuilderShaped builder = new RecipeBuilderShaped();
            builder.add("rro");
            builder.add("rdo");
            builder.add("rro");
            builder.map('r', Items.REDSTONE);
            builder.map('o', Blocks.OBSIDIAN);
            builder.map('d', Items.DIAMOND);
            builder.setResult(new ItemStack(BCBlocks.SILICON_LASER));
            builder.register();
        }

        if (BCItems.SILICON_REDSTONE_CLIPSET != null) {
            addChipsetAssembly(1, null, EnumRedstoneChipset.RED);
            addChipsetAssembly(2, Items.IRON_INGOT, EnumRedstoneChipset.IRON);
            addChipsetAssembly(4, Items.GOLD_INGOT, EnumRedstoneChipset.GOLD);
            addChipsetAssembly(6, Items.QUARTZ, EnumRedstoneChipset.QUARTZ);
            addChipsetAssembly(8, Items.DIAMOND, EnumRedstoneChipset.DIAMOND);
        }
    }

    private static void addChipsetAssembly(int multiplier, Item additional, EnumRedstoneChipset type) {
        ItemStack output = type.getStack();
        ImmutableSet.Builder<ItemStack> inputs = ImmutableSet.builder();
        inputs.add(new ItemStack(Items.REDSTONE));
        if (additional != null) {
            inputs.add(new ItemStack(additional));
        }

        AssemblyRecipe recp = new AssemblyRecipe(multiplier * 10_000 * MjAPI.MJ, inputs.build(), output);
        AssemblyRecipeRegistry.INSTANCE.addRecipe(recp);
    }
}
