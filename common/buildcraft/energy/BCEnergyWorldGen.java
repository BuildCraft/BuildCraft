package buildcraft.energy;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.generation.BiomeInitializer;
import buildcraft.energy.generation.BiomeOilDesert;
import buildcraft.energy.generation.BiomeOilOcean;
import buildcraft.energy.generation.OilGenerator;

public class BCEnergyWorldGen {
    public static void preInit() {
        GameRegistry.register(BiomeOilOcean.INSTANCE);
        GameRegistry.register(BiomeOilDesert.INSTANCE);
        BiomeDictionary.addTypes(BiomeOilOcean.INSTANCE, BiomeDictionary.Type.OCEAN);
        BiomeDictionary.addTypes(BiomeOilDesert.INSTANCE, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY,
            BiomeDictionary.Type.SANDY);

        if (BCCoreConfig.worldGen && BCEnergyConfig.enableOilGeneration) {
            GameRegistry.registerWorldGenerator(OilGenerator.INSTANCE, 0);
            MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
        }
    }
}
