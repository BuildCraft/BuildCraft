package buildcraft.lib.recipe.programming;

import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class ProgrammingRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<IProgrammingRecipe> {
    public static final ProgrammingRecipeSerializer INSTANCE;

    static {
        INSTANCE = new ProgrammingRecipeSerializer();
        INSTANCE.setRegistryName(IProgrammingRecipe.TYPE_ID);
    }

    @Override
    public IProgrammingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");
        ItemStack output = JsonUtil.deSerializeItemStack(json.getAsJsonObject("output"));
        long energyCost = GsonHelper.getAsLong(json, "energyCost");
        return new BoardProgrammingRecipe(recipeId, output, energyCost);
    }

    public static void toJson(ProgrammingRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", IProgrammingRecipe.TYPE_ID.toString());
        json.add("output", JsonUtil.serializeItemStack(builder.getOutput()));
        json.addProperty("energyCost", builder.getEnergyCost());
    }

    @Nullable
    @Override
    public IProgrammingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        ItemStack output = buffer.readItem();
        long energyCost = buffer.readLong();
        return new BoardProgrammingRecipe(id, output, energyCost);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, IProgrammingRecipe recipe) {
        buffer.writeResourceLocation(recipe.getId());
        buffer.writeItem(recipe.getOutput());
        buffer.writeLong(recipe.getEnergyCost());
    }
}
