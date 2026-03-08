package buildcraft.silicon.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class FacadeSwapRecipeSerializer extends SpecialRecipeSerializer<FacadeSwapRecipe> {
    // Calen
    public static final FacadeSwapRecipeSerializer INSTANCE;

    static {
        INSTANCE = new FacadeSwapRecipeSerializer(id -> FacadeSwapRecipe.INSTANCE);
        INSTANCE.setRegistryName(FacadeSwapRecipe.TYPE_ID);
    }

    public FacadeSwapRecipeSerializer(Function<ResourceLocation, FacadeSwapRecipe> constructor) {
        super(constructor);
    }

    public static void toJson(FacadeSwapRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", FacadeSwapRecipe.TYPE_ID.toString());
    }
}
