package buildcraft.energy.generation;

import net.minecraft.world.biome.BiomeDesert;

public final class BiomeOilDesert extends BiomeDesert {
    public static final BiomeOilDesert INSTANCE = new BiomeOilDesert();

    public BiomeOilDesert() {
        super(
                new BiomeProperties("Desert Oil Field")
                        .setBaseHeight(0.125F)
                        .setHeightVariation(0.05F)
                        .setTemperature(2.0F)
                        .setRainfall(0.0F)
                        .setRainDisabled()
        );
        setRegistryName("oil_desert");
    }
}
