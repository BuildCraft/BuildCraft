package buildcraft.lib.recipe.refinery;

import buildcraft.api.BCModules;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class HeatExchangeRecipeBuilder {
    public final EnumHeatExchangeRecipeType type;
    public final FluidStack in;
    public final FluidStack out;
    public final int heatFrom, heatTo;

    private HeatExchangeRecipeBuilder(EnumHeatExchangeRecipeType type, FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        this.type = type;
        this.in = in;
        this.out = out;
        this.heatFrom = heatFrom;
        this.heatTo = heatTo;
    }

    public static HeatExchangeRecipeBuilder heatable(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return new HeatExchangeRecipeBuilder(EnumHeatExchangeRecipeType.HEATABLE, in, out, heatFrom, heatTo);
    }

    public static HeatExchangeRecipeBuilder coolable(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return new HeatExchangeRecipeBuilder(EnumHeatExchangeRecipeType.COOLABLE, in, out, heatFrom, heatTo);
    }

    public void save(Consumer<IFinishedRecipe> consumer, String name) {
        consumer.accept(new HeatExchangeRecipeResult(name));
    }

    class HeatExchangeRecipeResult implements IFinishedRecipe {
        private final String name;

        public HeatExchangeRecipeResult(String name) {
            this.name = name;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            HeatExchangeRecipeSerializer.toJson(HeatExchangeRecipeBuilder.this, json);
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(BCModules.ENERGY.getModId(), "heat_exchange/" + type.getlowerName() + "/" + name);
        }

        @Override
        public IRecipeSerializer<IHeatExchangerRecipe> getType() {
            return HeatExchangeRecipeSerializer.HEATABLE;
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
