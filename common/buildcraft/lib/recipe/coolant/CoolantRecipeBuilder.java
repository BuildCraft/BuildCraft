package buildcraft.lib.recipe.coolant;

import buildcraft.api.BCModules;
import buildcraft.api.fuels.EnumCoolantType;
import buildcraft.api.fuels.ICoolant;
import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CoolantRecipeBuilder {
    public final FluidStack fluid;
    public final ItemStack solid;
    public final float degreesCoolingPerMb;
    public final float multiplier;
    public final EnumCoolantType type;

    private CoolantRecipeBuilder(FluidStack fluid, ItemStack solid, float degreesCoolingPerMb, float multiplier, EnumCoolantType type) {
        this.fluid = fluid;
        this.solid = solid;
        this.degreesCoolingPerMb = degreesCoolingPerMb;
        this.multiplier = multiplier;
        this.type = type;
    }

    public static CoolantRecipeBuilder fluidCoolant(FluidStack fluid, float degreesCoolingPerMb) {
        return new CoolantRecipeBuilder(fluid, null, degreesCoolingPerMb, -1, EnumCoolantType.FLUID);
    }

    public static CoolantRecipeBuilder solidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
        return new CoolantRecipeBuilder(fluid, solid, -1, multiplier, EnumCoolantType.SOLID);
    }

    public void save(Consumer<FinishedRecipe> consumer, String name) {
        consumer.accept(new CoolantRecipeBuilder.CoolantRecipeResult(name));
    }

    class CoolantRecipeResult implements FinishedRecipe {
        private final String name;

        public CoolantRecipeResult(String name) {
            this.name = name;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            CoolantRecipeSerializer.toJson(CoolantRecipeBuilder.this, json);
        }

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(BCModules.ENERGY.getModId(), "coolant/" + name);
        }

        @Override
        public RecipeSerializer<ICoolant> getType() {
            return CoolantRecipeSerializer.INSTANCE;
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
