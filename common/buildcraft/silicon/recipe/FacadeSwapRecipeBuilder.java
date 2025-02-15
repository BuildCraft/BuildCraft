package buildcraft.silicon.recipe;

import buildcraft.silicon.BCSilicon;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class FacadeSwapRecipeBuilder {
    private final String name;

    private FacadeSwapRecipeBuilder(String name) {
        this.name = name;
    }

    public static FacadeSwapRecipeBuilder swap(String name) {
        return new FacadeSwapRecipeBuilder(name);
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new FacadeSwapRecipeBuilder.FacadeSwapRecipeResult(BCSilicon.MODID, name));
    }

    class FacadeSwapRecipeResult implements FinishedRecipe {
        private final String namespace;
        private final String name;

        public FacadeSwapRecipeResult(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            FacadeSwapRecipeSerializer.toJson(FacadeSwapRecipeBuilder.this, json);
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(namespace, "facade_swap/" + name);
        }

        @Override
        public RecipeSerializer<FacadeSwapRecipe> getType() {
            return FacadeSwapRecipeSerializer.INSTANCE;
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
