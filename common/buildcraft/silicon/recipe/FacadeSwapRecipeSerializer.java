package buildcraft.silicon.recipe;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class FacadeSwapRecipeSerializer extends SimpleCraftingRecipeSerializer<FacadeSwapRecipe> {
    // Calen
    public static final FacadeSwapRecipeSerializer INSTANCE;

    static {
        INSTANCE = new FacadeSwapRecipeSerializer((id, category) -> FacadeSwapRecipe.INSTANCE);
    }

    public FacadeSwapRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<FacadeSwapRecipe> constructor) {
        super(constructor);
    }

    public static void toJson(FacadeSwapRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", FacadeSwapRecipe.TYPE_ID.toString());
    }
}
