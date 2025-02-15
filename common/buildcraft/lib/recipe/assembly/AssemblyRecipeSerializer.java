package buildcraft.lib.recipe.assembly;

import buildcraft.api.recipes.EnumAssemblyRecipeType;
import buildcraft.api.recipes.IAssemblyRecipe;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.misc.JsonUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class AssemblyRecipeSerializer implements RecipeSerializer<IAssemblyRecipe> {
    public static final AssemblyRecipeSerializer INSTANCE;

    static {
        INSTANCE = new AssemblyRecipeSerializer();
    }

    @Override
    public IAssemblyRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");
        EnumAssemblyRecipeType subType = EnumAssemblyRecipeType.valueOf(GsonHelper.getAsString(json, "subType"));
        IAssemblyRecipe recipe = null;
        switch (subType) {
            case BASIC:
                long requiredMicroJoules = json.get("requiredMicroJoules").getAsLong();
                JsonArray requiredStacks = json.get("requiredStacks").getAsJsonArray();
                List<IngredientStack> requiredStacksList = Lists.newArrayList();
                requiredStacks.forEach(j -> requiredStacksList.add(JsonUtil.deSerializeIngredientStack(j.getAsJsonObject())));
                ItemStack output = JsonUtil.deSerializeItemStack(json.getAsJsonObject("output"));
                recipe = new AssemblyRecipeBasic(recipeId, requiredMicroJoules, ImmutableSet.copyOf(requiredStacksList), output);
                break;
            case FACADE:
                recipe = AssemblyRecipeRegistry.FACADE_ASSEMBLY_RECIPE;
                break;
        }
        return recipe;
    }

    public static void toJson(AssemblyRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", AssemblyRecipe.TYPE_ID.toString());
        json.addProperty("subType", builder.type.name());
        switch (builder.type) {
            case BASIC:
                json.addProperty("requiredMicroJoules", builder.requiredMicroJoules);
                JsonArray requiredStacks = new JsonArray();
                builder.requiredStacks.forEach(ingredientStack -> requiredStacks.add(JsonUtil.serializeIngredientStack(ingredientStack)));
                json.add("requiredStacks", requiredStacks);
                json.add("output", JsonUtil.serializeItemStack(builder.output));
                break;
            case FACADE:
                break;
        }
    }

    @Nullable
    @Override
    public IAssemblyRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        IAssemblyRecipe recipe = null;
        switch (buffer.readEnum(EnumAssemblyRecipeType.class)) {
            case BASIC:
                long requiredMicroJoules = buffer.readLong();
                Set<IngredientStack> requiredStacks = Sets.newHashSet();
                int ingredientSize = buffer.readInt();
                for (int index = 0; index < ingredientSize; index++) {
                    requiredStacks.add(IngredientStack.fromNetwork(buffer));
                }
                ItemStack output = buffer.readItem();
                recipe = new AssemblyRecipeBasic(recipeId, requiredMicroJoules, ImmutableSet.copyOf(requiredStacks), output);
                break;
            case FACADE:
                recipe = AssemblyRecipeRegistry.FACADE_ASSEMBLY_RECIPE;
        }
        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, IAssemblyRecipe recipe) {
        if (recipe instanceof AssemblyRecipeBasic) {
            buffer.writeEnum(EnumAssemblyRecipeType.BASIC);
            buffer.writeLong(recipe.getRequiredMicroJoules());
            Set<IngredientStack> requiredStacks = recipe.getRequiredIngredientStacks();
            buffer.writeInt(requiredStacks.size());
            requiredStacks.forEach(ingredientStack -> ingredientStack.toNetwork(buffer));
            ItemStack output = recipe.getOutput().toArray(new ItemStack[0])[0];
            buffer.writeItemStack(output, false);
        } else if (recipe instanceof IFacadeAssemblyRecipes) {
            buffer.writeEnum(EnumAssemblyRecipeType.FACADE);
        }
    }
}
