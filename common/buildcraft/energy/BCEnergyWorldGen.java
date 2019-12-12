package buildcraft.energy;

import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.generation.BiomeInitializer;
import buildcraft.energy.generation.BiomeOilDesert;
import buildcraft.energy.generation.BiomeOilOcean;
import buildcraft.energy.generation.OilGenerator;

@Mod.EventBusSubscriber(modid = BCEnergy.MODID)
public class BCEnergyWorldGen {
    public static void init() {
        if (BCEnergyConfig.enableOilOceanBiome) {
            BiomeDictionary.addTypes(
                BiomeOilOcean.INSTANCE,
                BiomeDictionary.Type.OCEAN
            );
        }
        if (BCEnergyConfig.enableOilDesertBiome) {
            BiomeDictionary.addTypes(
                BiomeOilDesert.INSTANCE,
                BiomeDictionary.Type.HOT,
                BiomeDictionary.Type.DRY,
                BiomeDictionary.Type.SANDY
            );
        }
        if (BCCoreConfig.worldGen) {
            if (BCEnergyConfig.enableOilGeneration) {
                MinecraftForge.EVENT_BUS.register(OilGenerator.class);
            }
            if (BCEnergyConfig.enableOilOceanBiome || BCEnergyConfig.enableOilDesertBiome) {
                MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
            }
        }
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        if (BCEnergyConfig.enableOilDesertBiome) {
            event.getRegistry().register(new BiomeOilOcean());
        }
        if (BCEnergyConfig.enableOilDesertBiome) {
            event.getRegistry().register(new BiomeOilDesert());
        }
    }
}
