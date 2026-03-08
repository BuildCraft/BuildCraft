package buildcraft.lib.recipe.fuel;

import buildcraft.api.BCModules;
import buildcraft.api.fuels.IFuel;
import buildcraft.lib.misc.StackUtil;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class FuelRecipeBuilder {
    public final FluidStack fluid;
    public final long powerPerCycle;
    public final int totalBurningTime;
    public final boolean dirty;
    public final FluidStack residue;

    private FuelRecipeBuilder(FluidStack fluid, long powerPerCycle, int totalBurningTime, boolean dirty, FluidStack residue) {
        this.fluid = fluid;
        this.powerPerCycle = powerPerCycle;
        this.totalBurningTime = totalBurningTime;
        this.dirty = dirty;
        this.residue = residue;
    }

    public static FuelRecipeBuilder fuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
        return new FuelRecipeBuilder(fluid, powerPerCycle, totalBurningTime, false, StackUtil.EMPTY_FLUID);
    }

    public static FuelRecipeBuilder dirtyFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime, FluidStack residue) {
        return new FuelRecipeBuilder(fluid, powerPerCycle, totalBurningTime, true, residue);
    }

    public void save(Consumer<IFinishedRecipe> consumer, String name) {
        consumer.accept(new FuelRecipeBuilder.FuelRecipeResult(name));
    }

    class FuelRecipeResult implements IFinishedRecipe {
        private final String name;

        public FuelRecipeResult(String name) {
            this.name = name;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            FuelRecipeSerializer.toJson(FuelRecipeBuilder.this, json);
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(BCModules.ENERGY.getModId(), "fuel/" + name);
        }

        @Override
        public IRecipeSerializer<IFuel> getType() {
            return FuelRecipeSerializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
