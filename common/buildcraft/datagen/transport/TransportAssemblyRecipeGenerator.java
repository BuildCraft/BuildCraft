package buildcraft.datagen.transport;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.assembly.AssemblyRecipeBuilder;
import buildcraft.transport.BCTransport;
import buildcraft.transport.BCTransportItems;
import com.google.common.collect.ImmutableSet;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class TransportAssemblyRecipeGenerator extends RecipeProvider {
    private final ExistingFileHelper existingFileHelper;

    public TransportAssemblyRecipeGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        if (BCTransportItems.wire != null) {
            for (DyeColor color : ColourUtil.COLOURS) {
//                String name = String.format("wire-%s", color.getUnlocalizedName());
                String name = String.format("wire_%s", color.getSerializedName());
//                ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(ColourUtil.getDyeName(color)));
                ImmutableSet<IngredientStack> input = ImmutableSet.of(IngredientStack.of(Tags.Items.DUSTS_REDSTONE), IngredientStack.of(color.getTag()));
//                AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(name, 10_000 * MjAPI.MJ, input, new ItemStack(BCTransportItems.wire, 8, color.getMetadata())));
                ItemStack wireStack = new ItemStack(BCTransportItems.wire.get(), 8);
                ColourUtil.addColourTagToStack(wireStack, color);
                AssemblyRecipeBuilder.basic(10_000 * MjAPI.MJ, input, wireStack).save(consumer, BCTransport.MODID, name);
            }
        }
    }

    @Override
    public String getName() {
        return "BuildCraft Transport Assembly Recipe Generator";
    }
}
