package buildcraft.lib.recipe.programming;

import buildcraft.api.BCModules;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.api.recipes.IngredientStack;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ProgrammingRecipeBuilder {
    private final IngredientStack input;
    private final ItemStack output;
    private final long energyCost;

    private ProgrammingRecipeBuilder(IngredientStack input, ItemStack output, long energyCost) {
        this.input = input;
        this.output = output;
        this.energyCost = energyCost;
    }

    public IngredientStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public long getEnergyCost() {
        return energyCost;
    }

    public static ProgrammingRecipeBuilder programming(IngredientStack input, ItemStack output, long energyCost) {
        return new ProgrammingRecipeBuilder(input, output, energyCost);
    }

    public void save(Consumer<IFinishedRecipe> consumer, String name) {
        consumer.accept(new ProgrammingRecipeResult(BCModules.ROBOTICS.getModId(), name));
    }

    public void save(Consumer<IFinishedRecipe> consumer, String namespace, String name) {
        consumer.accept(new ProgrammingRecipeResult(namespace, name));
    }

    class ProgrammingRecipeResult implements IFinishedRecipe {
        private final String namespace;
        private final String name;

        public ProgrammingRecipeResult(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            ProgrammingRecipeSerializer.toJson(ProgrammingRecipeBuilder.this, json);
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(namespace, "programming/" + name);
        }

        @Override
        public IRecipeSerializer<IProgrammingRecipe> getType() {
            return ProgrammingRecipeSerializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
