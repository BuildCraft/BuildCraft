package buildcraft.lib.recipe.integration;

import buildcraft.api.BCModules;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.lib.misc.JsonUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.List;

public class IntegrationRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<IntegrationRecipe> {
    public static final IntegrationRecipeSerializer INSTANCE;

    static {
        INSTANCE = new IntegrationRecipeSerializer();
        INSTANCE.setRegistryName(IntegrationRecipe.TYPE_ID);
    }

    private IntegrationRecipeSerializer() {
    }

    @Override
    public IntegrationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        long requiredMicroJoules = GsonHelper.getAsLong(json, "requiredMicroJoules");
        IngredientStack centerStack = JsonUtil.deSerializeIngredientStack(GsonHelper.getAsJsonObject(json, "centerStack"));
        JsonArray requirementsJson = GsonHelper.getAsJsonArray(json, "requirements");
        List<IngredientStack> requirements = Lists.newArrayList();
        requirementsJson.forEach(j -> requirements.add(JsonUtil.deSerializeIngredientStack(j.getAsJsonObject())));
        ItemStack output = JsonUtil.deSerializeItemStack(json.getAsJsonObject("output"));

        return new IntegrationRecipeBasic(recipeId, requiredMicroJoules, centerStack, ImmutableList.copyOf(requirements), output);
    }

    public static void toJson(IntegrationRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", BCModules.SILICON.getModId() + ":integration");

        json.addProperty("requiredMicroJoules", builder.requiredMicroJoules);
        json.add("centerStack", JsonUtil.serializeIngredientStack(builder.centerStack));
        JsonArray requirementsJson = new JsonArray();
        builder.requirements.forEach(ingredientStack -> requirementsJson.add(JsonUtil.serializeIngredientStack(ingredientStack)));
        json.add("requirements", requirementsJson);
        json.add("output", JsonUtil.serializeItemStack(builder.exampleOutput));
    }

    @Nullable
    @Override
    public IntegrationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        long requiredMicroJoules = buffer.readLong();
        IngredientStack centerStack = IngredientStack.fromNetwork(buffer);
        int requirementsSize = buffer.readInt();
        List<IngredientStack> requirements = Lists.newArrayList();
        for (int i = 0; i < requirementsSize; i++) {
            requirements.add(IngredientStack.fromNetwork(buffer));
        }
        ItemStack output = buffer.readItem();

        return new IntegrationRecipeBasic(recipeId, requiredMicroJoules, centerStack, ImmutableList.copyOf(requirements), output);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, IntegrationRecipe recipe) {
        buffer.writeLong(recipe.getRequiredMicroJoules());
        recipe.getCenterStack().toNetwork(buffer);
        ImmutableList<IngredientStack> requirements = recipe.getRequirements();
        buffer.writeInt(requirements.size());
        requirements.forEach(ingredientStack -> ingredientStack.toNetwork(buffer));
        buffer.writeItem(recipe.getExampleOutput());
    }
}
