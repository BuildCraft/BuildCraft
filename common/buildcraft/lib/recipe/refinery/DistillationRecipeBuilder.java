package buildcraft.lib.recipe.refinery;

import buildcraft.api.BCModules;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import com.google.gson.JsonObject;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class DistillationRecipeBuilder {
    public final long powerRequired;
    public final FluidStack in;
    public final FluidStack outGas;
    public final FluidStack outLiquid;

    private DistillationRecipeBuilder(long powerRequired, FluidStack in, FluidStack outGas, FluidStack outLiquid) {
        this.powerRequired = powerRequired;
        this.in = in;
        this.outGas = outGas;
        this.outLiquid = outLiquid;
    }

    public static DistillationRecipeBuilder distillation(long powerRequired, FluidStack in, FluidStack outGas, FluidStack outLiquid) {
        return new DistillationRecipeBuilder(powerRequired, in, outGas, outLiquid);
    }

    public void save(Consumer<IFinishedRecipe> consumer, String name) {
        consumer.accept(new DistillationRecipeBuilder.DistillationRecipeResult(name));
    }

    class DistillationRecipeResult implements IFinishedRecipe {
        private final String name;

        public DistillationRecipeResult(String name) {
            this.name = name;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            DistillationRecipeSerializer.toJson(DistillationRecipeBuilder.this, json);
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(BCModules.ENERGY.getModId(), "distillation/" + name);
        }

        @Override
        public IRecipeSerializer<IDistillationRecipe> getType() {
            return DistillationRecipeSerializer.INSTANCE;
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
