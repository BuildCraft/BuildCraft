package buildcraft.lib.recipe.integration;

import buildcraft.api.BCModules;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class IntegrationRecipeBuilder {
    public final long requiredMicroJoules;
    public final IngredientStack centerStack;
    public final ImmutableList<IngredientStack> requirements;
    public final ItemStack exampleOutput;

    private IntegrationRecipeBuilder(long requiredMicroJoules, IngredientStack centerStack, ImmutableList<IngredientStack> requirements, @Nonnull ItemStack exampleOutput) {
        this.requiredMicroJoules = requiredMicroJoules;
        this.centerStack = centerStack;
        this.requirements = requirements;
        this.exampleOutput = exampleOutput;
    }

    public static IntegrationRecipeBuilder basic(long requiredMicroJoules, IngredientStack centerStack, ImmutableList<IngredientStack> requirements, @Nonnull ItemStack output) {
        return new IntegrationRecipeBuilder(requiredMicroJoules, centerStack, requirements, output);
    }

    public void save(Consumer<IFinishedRecipe> consumer, String name) {
        consumer.accept(new IntegrationRecipeResult(BCModules.SILICON.getModId(), name));
    }

    public void save(Consumer<IFinishedRecipe> consumer, String namespace, String name) {
        consumer.accept(new IntegrationRecipeResult(namespace, name));
    }

    class IntegrationRecipeResult implements IFinishedRecipe {
        private final String namespace;
        private final String name;

        public IntegrationRecipeResult(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            IntegrationRecipeSerializer.toJson(IntegrationRecipeBuilder.this, json);
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(namespace, "integration/" + name);
        }

        @Override
        public IRecipeSerializer<IntegrationRecipe> getType() {
            return IntegrationRecipeSerializer.INSTANCE;
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
