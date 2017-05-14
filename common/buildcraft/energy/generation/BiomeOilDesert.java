package buildcraft.energy.generation;

import net.minecraft.world.biome.BiomeDesert;

public final class BiomeOilDesert extends BiomeDesert {
    public static final BiomeOilDesert INSTANCE = new BiomeOilDesert();

    public BiomeOilDesert() {
        super(
                new BiomeProperties("Desert Oil Field")
                        .setRainDisabled()
                        .setTemperature(2.0F)
                        .setBaseHeight(0.125F)
        );
        setRegistryName("oil_desert");
    }
}
