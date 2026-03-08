package buildcraft.lib.recipe.fuel;

import buildcraft.api.fuels.IFuel;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class FuelRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<IFuel> {
    public static final FuelRecipeSerializer INSTANCE;

    static {
        INSTANCE = new FuelRecipeSerializer();
        INSTANCE.setRegistryName(IFuel.TYPE_ID);
    }

    @Override
    public FuelRegistry.Fuel fromJson(ResourceLocation recipeId, JsonObject json) {
        String type = GsonHelper.getAsString(json, "type");
        FluidStack fluid = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("fluid"));
        long powerPerCycle = json.get("powerPerCycle").getAsLong();
        int totalBurningTime = GsonHelper.getAsInt(json, "totalBurningTime");
        boolean dirty = GsonHelper.getAsBoolean(json, "dirty");
        if (dirty) {
            FluidStack residue = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("residue"));
            return new FuelRegistry.DirtyFuel(recipeId, fluid, powerPerCycle, totalBurningTime, residue);
        } else {
            return new FuelRegistry.Fuel(recipeId, fluid, powerPerCycle, totalBurningTime);
        }
    }

    public static void toJson(FuelRecipeBuilder builder, JsonObject json) {
        json.addProperty("type", IFuel.TYPE_ID.toString());
        json.add("fluid", JsonUtil.serializeFluidStack(builder.fluid));
        json.addProperty("powerPerCycle", builder.powerPerCycle);
        json.addProperty("totalBurningTime", builder.totalBurningTime);
        json.addProperty("dirty", builder.dirty);
        if (builder.dirty) {
            json.add("residue", JsonUtil.serializeFluidStack(builder.residue));
        }
    }

    @Nullable
    @Override
    public IFuel fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        FluidStack fluid = buffer.readFluidStack();
        long powerPerCycle = buffer.readLong();
        int totalBurningTime = buffer.readInt();
        boolean dirty = buffer.readBoolean();
        if (dirty) {
            FluidStack residue = buffer.readFluidStack();
            return new FuelRegistry.DirtyFuel(recipeId, fluid, powerPerCycle, totalBurningTime, residue);
        } else {
            return new FuelRegistry.Fuel(recipeId, fluid, powerPerCycle, totalBurningTime);
        }
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, IFuel recipe) {
        buffer.writeFluidStack(recipe.getFluid());
        buffer.writeLong(recipe.getPowerPerCycle());
        buffer.writeInt(recipe.getTotalBurningTime());
        buffer.writeBoolean(recipe instanceof FuelRegistry.DirtyFuel);
        if (recipe instanceof FuelRegistry.DirtyFuel dirtyFuel) {
            buffer.writeFluidStack(dirtyFuel.getResidue());
        }
    }
}
