package buildcraft.energy;

import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.core.BCLog;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.generation.BiomeInitializer;
import buildcraft.energy.generation.BiomeOilDesert;
import buildcraft.energy.generation.BiomeOilOcean;
import buildcraft.energy.generation.OilGenerator;

@Mod.EventBusSubscriber(modid = BCEnergy.MODID)
public class BCEnergyWorldGen {
    public static void init() {
        boolean log = OilGenerator.DEBUG_OILGEN_BASIC;
        if (BCEnergyConfig.enableOilOceanBiome) {
            BiomeDictionary.addTypes(
                BiomeOilOcean.INSTANCE,
                BiomeDictionary.Type.OCEAN
            );
            BCLog.logger.info("[energy.oilgen] Registered the ocean oil biome.");
        } else {
            BCLog.logger.info("[energy.oilgen] Not registering the ocean oil biome, as it has been disabled by the config file.");
        }
        if (BCEnergyConfig.enableOilDesertBiome) {
            BiomeDictionary.addTypes(
                BiomeOilDesert.INSTANCE,
                BiomeDictionary.Type.HOT,
                BiomeDictionary.Type.DRY,
                BiomeDictionary.Type.SANDY
            );
            BCLog.logger.info("[energy.oilgen] Registered the desert oil biome.");
        } else {
            BCLog.logger.info("[energy.oilgen] Not registering the desert oil biome, as it has been disabled by the config file.");
        }
        if (BCCoreConfig.worldGen) {
            if (BCEnergyConfig.enableOilGeneration) {
                MinecraftForge.EVENT_BUS.register(OilGenerator.class);
                BCLog.logger.info("[energy.oilgen] Registered the oil spout generator");
            } else {
                BCLog.logger.info("[energy.oilgen] Not registering the oil spout generator, as it has been disabled by the config file.");
            }
            if (BCEnergyConfig.enableOilOceanBiome || BCEnergyConfig.enableOilDesertBiome) {
                MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
                BCLog.logger.info("[energy.oilgen] Registered the oil biome initiializer");
            } else {
                BCLog.logger.info("[energy.oilgen] Not registering the oil biome initiializer, as it has been disabled by the config file.");
            }
        } else {
            BCLog.logger.info("[energy.oilgen] Not registering any world-gen, as everything has been disabled by the config file.");
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
