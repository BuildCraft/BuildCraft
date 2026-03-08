package buildcraft.lib.fluid;

import buildcraft.energy.BCEnergyFluids;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.BiFunction;

// Calen add
public class BCFluidAttributes extends FluidAttributes {
    protected BCFluidAttributes(Builder builder, Fluid fluid) {
        super(builder, fluid);
        this.heat = builder.heat;
        this.heatable = builder.heatable;
        this.colour = builder.colour;
        this.light = builder.light;
        this.dark = builder.dark;
    }

    private int colour, light, dark;

    public int getLightColour() {
        return light;
    }

    public int getDarkColour() {
        return dark;
    }

    private int heat;

    public BCFluidAttributes setHeat(int heat) {
        this.heat = heat;
        return this;
    }

    public int getHeat() {
        return heat;
    }

    private boolean heatable;

    public BCFluidAttributes setHeatable(boolean value) {
        heatable = value;
        return this;
    }

    public boolean isHeatable() {
        return heatable;
    }

    @Override
    public Component getDisplayName(FluidStack stack) {
        return getDisplayName();
    }

    public Component getDisplayName() {
        if (heat <= 0 && !isHeatable()) {
            return getBareLocalizedName();
        }
        Component name = getBareLocalizedName();
//        return new TextComponent(LocaleUtil.localize("buildcraft.fluid.heat_" + heat, name));
        return new TranslatableComponent(BCEnergyFluids.FLUID_TRANSLATION_PREFIX + heat, name);
    }

    public Component getBareLocalizedName() {
//        return super.getLocalizedName(stack);
        return new TranslatableComponent(getTranslationKey());
    }

    public static BCFluidAttributes.Builder builder(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return new BCFluidAttributes.Builder(stillTexture, flowingTexture, BCFluidAttributes::new);
    }

    public static class Builder extends FluidAttributes.Builder {
        private BiFunction<BCFluidAttributes.Builder, Fluid, BCFluidAttributes> factory;

        protected Builder(ResourceLocation stillTexture, ResourceLocation flowingTexture, BiFunction<BCFluidAttributes.Builder, Fluid, BCFluidAttributes> factory) {
            super(stillTexture, flowingTexture, (builder, fluid) -> factory.apply((BCFluidAttributes.Builder) builder, fluid));
            this.factory = factory;
        }

        private int colour = 0xFFFFFFFF, light = 0xFF_FF_FF_FF, dark = 0xFF_FF_FF_FF;

        public Builder setColour(int light, int dark) {
            this.light = light;
            this.dark = dark;
            this.colour = 0xFF_FF_FF_FF;
            return this;
        }

        private int heat;

        public Builder setHeat(int heat) {
            this.heat = heat;
            return this;
        }

        private boolean heatable;

        public Builder setHeatable(boolean value) {
            heatable = value;
            return this;
        }

        public BCFluidAttributes build(Fluid fluid) {
            return factory.apply(this, fluid);
        }
    }
}
