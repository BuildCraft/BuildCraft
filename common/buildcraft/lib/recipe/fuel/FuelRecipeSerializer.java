package buildcraft.lib.recipe.fuel;

import buildcraft.api.fuels.IFuel;
import buildcraft.lib.misc.JsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class FuelRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<IFuel> {
    public static final FuelRecipeSerializer INSTANCE;

    static {
        INSTANCE = new FuelRecipeSerializer();
        INSTANCE.setRegistryName(IFuel.TYPE_ID);
    }

    @Override
    public FuelRegistry.Fuel fromJson(ResourceLocation recipeId, JsonObject json) {
        String type = JSONUtils.getAsString(json, "type");
        FluidStack fluid = JsonUtil.deSerializeFluidStack(json.getAsJsonObject("fluid"));
        long powerPerCycle = json.get("powerPerCycle").getAsLong();
        int totalBurningTime = JSONUtils.getAsInt(json, "totalBurningTime");
        boolean dirty = JSONUtils.getAsBoolean(json, "dirty");
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
    public IFuel fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
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
    public void toNetwork(PacketBuffer buffer, IFuel recipe) {
        buffer.writeFluidStack(recipe.getFluid());
        buffer.writeLong(recipe.getPowerPerCycle());
        buffer.writeInt(recipe.getTotalBurningTime());
        buffer.writeBoolean(recipe instanceof FuelRegistry.DirtyFuel);
        if (recipe instanceof FuelRegistry.DirtyFuel) {
            FuelRegistry.DirtyFuel dirtyFuel = (FuelRegistry.DirtyFuel) recipe;
            buffer.writeFluidStack(dirtyFuel.getResidue());
        }
    }
}
