package buildcraft.energy;

import net.minecraft.block.material.MapColor;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.fluid.BCFluid;
import buildcraft.lib.fluid.FluidManager;

public class BCEnergyFluids {

    public static BCFluid oil;

    public static void preInit() {
        oil = create("oil", "oil_still", "oil_flow");
        oil.setFlamable(true);
        oil.setDensity(800);
        oil.setViscosity(10000);
        oil.setLightOpacity(8);
        oil.setMapColour(MapColor.BLACK);
        FluidManager.register(oil);
    }

    private static BCFluid create(String name, String still, String flow) {
        return new BCFluid(name, new ResourceLocation("buildcraftenergy", "blocks/fluids/" + still), new ResourceLocation("buildcraftenergy", "blocks/fluids/" + flow));
    }
}
