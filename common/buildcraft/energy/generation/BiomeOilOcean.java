package buildcraft.energy.generation;

import net.minecraft.world.biome.BiomeOcean;

public final class BiomeOilOcean extends BiomeOcean {
    public static final BiomeOilOcean INSTANCE = new BiomeOilOcean();

    public BiomeOilOcean() {
        super(
                new BiomeProperties("Ocean Oil Field")
                        .setRainDisabled()
                        .setBaseHeight(-1.0F)
                        .setHeightVariation(0.1F)
        );
        setRegistryName("oil_ocean");
    }
}
