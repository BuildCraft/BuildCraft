package buildcraft.lib.recipe.refinery;

import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class DistillationRecipeSerializer implements RecipeSerializer<IDistillationRecipe> {
    public static final DistillationRecipeSerializer INSTANCE;

    static {
        INSTANCE = new DistillationRecipeSerializer();
    }

    @Override
    public IDistillationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");
        long powerRequired = json.get("powerRequired").getAsLong();
        FluidStack in = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("in"));
        FluidStack outGas = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("outGas"));
        FluidStack outLiquid = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("outLiquid"));
        return new RefineryRecipeRegistry.DistillationRecipe(recipeId, powerRequired, in, outGas, outLiquid);
    }

    public static void toJson(DistillationRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", IDistillationRecipe.TYPE_ID.toString());
        json.addProperty("powerRequired", builder.powerRequired);
        json.add("in", JsonUtil.serializeFluidStack(builder.in));
        json.add("outGas", JsonUtil.serializeFluidStack(builder.outGas));
        json.add("outLiquid", JsonUtil.serializeFluidStack(builder.outLiquid));
    }

    @Nullable
    @Override
    public IDistillationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        long powerRequired = buffer.readLong();
        FluidStack in = buffer.readFluidStack();
        FluidStack outGas = buffer.readFluidStack();
        FluidStack outLiquid = buffer.readFluidStack();
        return new RefineryRecipeRegistry.DistillationRecipe(recipeId, powerRequired, in, outGas, outLiquid);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, IDistillationRecipe recipe) {
        buffer.writeLong(recipe.powerRequired());
        buffer.writeFluidStack(recipe.in());
        buffer.writeFluidStack(recipe.outGas());
        buffer.writeFluidStack(recipe.outLiquid());
    }
}
