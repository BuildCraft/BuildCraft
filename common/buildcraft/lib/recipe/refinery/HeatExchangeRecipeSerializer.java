package buildcraft.lib.recipe.refinery;

import buildcraft.api.BCModules;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import buildcraft.lib.misc.JsonUtil;
import buildcraft.lib.recipe.refinery.RefineryRecipeRegistry.CoolableRecipe;
import buildcraft.lib.recipe.refinery.RefineryRecipeRegistry.HeatableRecipe;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class HeatExchangeRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<IHeatExchangerRecipe> {
    public static final HeatExchangeRecipeSerializer HEATABLE;
    public static final HeatExchangeRecipeSerializer COOLABLE;

    static {
        HEATABLE = new HeatExchangeRecipeSerializer(EnumHeatExchangeRecipeType.HEATABLE);
        COOLABLE = new HeatExchangeRecipeSerializer(EnumHeatExchangeRecipeType.COOLABLE);
        HEATABLE.setRegistryName(HeatableRecipe.TYPE_ID);
        COOLABLE.setRegistryName(CoolableRecipe.TYPE_ID);
    }

    private final EnumHeatExchangeRecipeType type;

    private HeatExchangeRecipeSerializer(EnumHeatExchangeRecipeType type) {
        this.type = type;
    }

    @Override
    public IHeatExchangerRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String typeStr = JSONUtils.getAsString(json, "type").replace("heat_exchange/", "");
        FluidStack in = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("in"));
        FluidStack out = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("out"));
        int heatFrom = json.get("heatFrom").getAsInt();
        int heatTo = json.get("heatTo").getAsInt();
        switch (this.type) {
            case COOLABLE:
                return new CoolableRecipe(recipeId, in, out, heatFrom, heatTo);
            case HEATABLE:
                return new HeatableRecipe(recipeId, in, out, heatFrom, heatTo);
            default:
                throw new RuntimeException("Unexpected EnumHeatExchangeRecipeType: [" + this.type + "]");
        }
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
    public IHeatExchangerRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        FluidStack in = buffer.readFluidStack();
        FluidStack out = buffer.readFluidStack();
        int heatFrom = buffer.readInt();
        int heatTo = buffer.readInt();
        switch (this.type) {
            case COOLABLE:
                return new CoolableRecipe(recipeId, in, out, heatFrom, heatTo);
            case HEATABLE:
                return new HeatableRecipe(recipeId, in, out, heatFrom, heatTo);
            default:
                throw new RuntimeException("Unexpected EnumHeatExchangeRecipeType: [" + this.type + "]");
        }
    }

    @Override
    public void toNetwork(PacketBuffer buffer, IHeatExchangerRecipe recipe) {
        buffer.writeFluidStack(recipe.in());
        buffer.writeFluidStack(recipe.out());
        buffer.writeInt(recipe.heatFrom());
        buffer.writeInt(recipe.heatTo());
    }
}
