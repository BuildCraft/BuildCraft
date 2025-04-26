package buildcraft.lib.recipe.coolant;

import buildcraft.api.fuels.EnumCoolantType;
import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.IFluidCoolant;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.lib.misc.JsonUtil;
import buildcraft.lib.recipe.coolant.CoolantRegistry.FluidCoolant;
import buildcraft.lib.recipe.coolant.CoolantRegistry.SolidCoolant;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class CoolantRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ICoolant> {
    public static final CoolantRecipeSerializer INSTANCE;

    static {
        INSTANCE = new CoolantRecipeSerializer();
        INSTANCE.setRegistryName(ICoolant.TYPE_ID);
    }

    @Override
    public ICoolant fromJson(ResourceLocation recipeId, JsonObject json) {
        String type = JSONUtils.getAsString(json, "type");
        EnumCoolantType coolantType = EnumCoolantType.byName(JSONUtils.getAsString(json, "coolantType"));
        FluidStack fluid = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("fluid"));
        switch (coolantType) {
            case FLUID:
                float degreesCoolingPerMb = JSONUtils.getAsFloat(json, "degreesCoolingPerMb");
                return new FluidCoolant(recipeId, fluid, degreesCoolingPerMb);
            case SOLID:
                float multiplier = JSONUtils.getAsFloat(json, "multiplier");
                ItemStack solid = ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "solid"));
                return new SolidCoolant(recipeId, solid, fluid, multiplier);
        }
        throw new IllegalArgumentException("[energy.recipe] Unexpected Coolant Type!");
    }

    public static void toJson(CoolantRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", ICoolant.TYPE_ID.toString());
        json.addProperty("coolantType", builder.type.getLowerName());
        json.add("fluid", JsonUtil.serializeFluidStack(builder.fluid));
        if (builder.type == EnumCoolantType.SOLID) {
            json.addProperty("multiplier", builder.multiplier);
            json.add("solid", JsonUtil.serializeItemStack(builder.solid));
        } else {
            json.addProperty("degreesCoolingPerMb", builder.degreesCoolingPerMb);
        }
    }

    @Nullable
    @Override
    public ICoolant fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        EnumCoolantType coolantType = buffer.readEnum(EnumCoolantType.class);
        FluidStack fluid = buffer.readFluidStack();
        float floatValue = buffer.readFloat();
        switch (coolantType) {
            case FLUID:
                return new FluidCoolant(recipeId, fluid, floatValue);
            case SOLID:
                ItemStack solid = buffer.readItem();
                return new SolidCoolant(recipeId, solid, fluid, floatValue);
        }
        throw new IllegalArgumentException("[energy.recipe] Unexpected Coolant Type!");
    }

    @Override
    public void toNetwork(PacketBuffer buffer, ICoolant recipe) {
        EnumCoolantType coolantType = recipe.getCoolantType();
        buffer.writeEnum(coolantType);
        buffer.writeFluidStack(recipe.getFluid());
        switch (coolantType) {
            case FLUID:
                buffer.writeFloat(((IFluidCoolant) recipe).getDegreesCoolingPerMB());
                return;
            case SOLID:
                buffer.writeFloat(((ISolidCoolant) recipe).getMultiplier());
                buffer.writeItemStack(((ISolidCoolant) recipe).getSolid(), false);
                return;
        }
    }
}
