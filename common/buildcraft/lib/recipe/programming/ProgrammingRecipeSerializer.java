package buildcraft.lib.recipe.programming;

import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class ProgrammingRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<IProgrammingRecipe> {
    public static final ProgrammingRecipeSerializer INSTANCE;

    static {
        INSTANCE = new ProgrammingRecipeSerializer();
        INSTANCE.setRegistryName(IProgrammingRecipe.TYPE_ID);
    }

    @Override
    public IProgrammingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String type = JSONUtils.getAsString(json, "type");
        IngredientStack input = JsonUtil.deSerializeIngredientStack(json.getAsJsonObject("input"));
        ItemStack output = JsonUtil.deSerializeItemStack(json.getAsJsonObject("output"));
        long energyCost = JSONUtils.getAsLong(json, "energyCost");
        return new BoardProgrammingRecipe(recipeId, input, output, energyCost);
    }

    public static void toJson(ProgrammingRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", IProgrammingRecipe.TYPE_ID.toString());
        json.add("input", JsonUtil.serializeIngredientStack(builder.getInput()));
        json.add("output", JsonUtil.serializeItemStack(builder.getOutput()));
        json.addProperty("energyCost", builder.getEnergyCost());
    }

    @Nullable
    @Override
    public IProgrammingRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        IngredientStack input = IngredientStack.fromNetwork(buffer);
        ItemStack output = buffer.readItem();
        long energyCost = buffer.readLong();
        return new BoardProgrammingRecipe(id, input, output, energyCost);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, IProgrammingRecipe recipe) {
        buffer.writeResourceLocation(recipe.getId());
        recipe.getInput().toNetwork(buffer);
        buffer.writeItem(recipe.getOutput());
        buffer.writeLong(recipe.getEnergyCost());
    }
}
