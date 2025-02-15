package buildcraft.lib.recipe.refinery;

import buildcraft.api.BCModules;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import buildcraft.lib.misc.JsonUtil;
import buildcraft.lib.recipe.refinery.RefineryRecipeRegistry.CoolableRecipe;
import buildcraft.lib.recipe.refinery.RefineryRecipeRegistry.HeatableRecipe;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class HeatExchangeRecipeSerializer implements RecipeSerializer<IHeatExchangerRecipe> {
    public static final HeatExchangeRecipeSerializer HEATABLE;
    public static final HeatExchangeRecipeSerializer COOLABLE;

    static {
        HEATABLE = new HeatExchangeRecipeSerializer(EnumHeatExchangeRecipeType.HEATABLE);
        COOLABLE = new HeatExchangeRecipeSerializer(EnumHeatExchangeRecipeType.COOLABLE);
    }

    private final EnumHeatExchangeRecipeType type;

    private HeatExchangeRecipeSerializer(EnumHeatExchangeRecipeType type) {
        this.type = type;
    }

    @Override
    public IHeatExchangerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String typeStr = GsonHelper.getAsString(json, "type").replace("heat_exchange/", "");
        FluidStack in = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("in"));
        FluidStack out = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("out"));
        int heatFrom = json.get("heatFrom").getAsInt();
        int heatTo = json.get("heatTo").getAsInt();
        return switch (this.type) {
            case COOLABLE -> new CoolableRecipe(recipeId, in, out, heatFrom, heatTo);
            case HEATABLE -> new HeatableRecipe(recipeId, in, out, heatFrom, heatTo);
        };
    }

    public static void toJson(HeatExchangeRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", BCModules.FACTORY.getModId() + ":heat_exchange/" + builder.type.getlowerName());
        json.add("in", JsonUtil.serializeFluidStack(builder.in));
        json.add("out", JsonUtil.serializeFluidStack(builder.out));
        json.addProperty("heatFrom", builder.heatFrom);
        json.addProperty("heatTo", builder.heatTo);
    }

    @Nullable
    @Override
    public IHeatExchangerRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        FluidStack in = buffer.readFluidStack();
        FluidStack out = buffer.readFluidStack();
        int heatFrom = buffer.readInt();
        int heatTo = buffer.readInt();
        return switch (this.type) {
            case COOLABLE -> new CoolableRecipe(recipeId, in, out, heatFrom, heatTo);
            case HEATABLE -> new HeatableRecipe(recipeId, in, out, heatFrom, heatTo);
        };
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, IHeatExchangerRecipe recipe) {
        buffer.writeFluidStack(recipe.in());
        buffer.writeFluidStack(recipe.out());
        buffer.writeInt(recipe.heatFrom());
        buffer.writeInt(recipe.heatTo());
    }
}
