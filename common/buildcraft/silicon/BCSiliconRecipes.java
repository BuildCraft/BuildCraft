package buildcraft.silicon;

import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.silicon.item.ItemRedstoneChipset;
import com.google.common.collect.ImmutableSet;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class BCSiliconRecipes {
    public static void init() {
//        if (BCSiliconItems.waterproof != null) {
//            GameRegistry.addShapelessRecipe(new ItemStack(BCSiliconItems.waterproof), new ItemStack(Items.DYE, 1, 2));
//        }
//
//        if (Utils.isRegistered(BCSiliconBlocks.laser)) {
//            ItemStack out = new ItemStack(BCSiliconBlocks.laser);
//            RecipeBuilderShaped builder = new RecipeBuilderShaped(out);
//            builder.add("w w"); // TODO: diamond pipe in center of this line
//            builder.add("wcw");
//            builder.add("wpw");
//            builder.map('w', "plankWood");
//            builder.map('p', Blocks.PISTON);
//            builder.map('c', Blocks.CHEST);
//            GameRegistry.addRecipe(builder.build());
//        }

        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(10000000000L, ImmutableSet.of(new ItemStack(Items.REDSTONE)), new ItemStack(BCSiliconItems.redstoneChipset, 1, ItemRedstoneChipset.Type.RED.ordinal())));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(20000000000L, ImmutableSet.of(new ItemStack(Items.REDSTONE), new ItemStack(Items.IRON_INGOT)), new ItemStack(BCSiliconItems.redstoneChipset, 1, ItemRedstoneChipset.Type.IRON.ordinal())));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(40000000000L, ImmutableSet.of(new ItemStack(Items.REDSTONE), new ItemStack(Items.GOLD_INGOT)), new ItemStack(BCSiliconItems.redstoneChipset, 1, ItemRedstoneChipset.Type.GOLD.ordinal())));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(60000000000L, ImmutableSet.of(new ItemStack(Items.REDSTONE), new ItemStack(Items.QUARTZ)), new ItemStack(BCSiliconItems.redstoneChipset, 1, ItemRedstoneChipset.Type.QUARTZ.ordinal())));
        AssemblyRecipeRegistry.INSTANCE.addRecipe(new AssemblyRecipe(80000000000L, ImmutableSet.of(new ItemStack(Items.REDSTONE), new ItemStack(Items.DIAMOND)), new ItemStack(BCSiliconItems.redstoneChipset, 1, ItemRedstoneChipset.Type.DIAMOND.ordinal())));
    }
}
