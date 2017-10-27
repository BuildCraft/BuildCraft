package buildcraft.energy;

import net.minecraft.world.biome.Biome;

import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.core.BCCoreConfig;
import buildcraft.energy.generation.BiomeInitializer;
import buildcraft.energy.generation.BiomeOilDesert;
import buildcraft.energy.generation.BiomeOilOcean;
import buildcraft.energy.generation.OilGenerator;

@Mod.EventBusSubscriber(modid = BCEnergy.MODID)
public class BCEnergyWorldGen {
    public static void init() {
        BiomeDictionary.addTypes(
            BiomeOilOcean.INSTANCE,
            BiomeDictionary.Type.OCEAN
        );
        BiomeDictionary.addTypes(
            BiomeOilDesert.INSTANCE,
            BiomeDictionary.Type.HOT,
            BiomeDictionary.Type.DRY,
            BiomeDictionary.Type.SANDY
        );
        if (BCCoreConfig.worldGen && BCEnergyConfig.enableOilGeneration) {
            GameRegistry.registerWorldGenerator(OilGenerator.INSTANCE, 0);
            MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
        }
    }

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(
            new BiomeOilOcean(),
            new BiomeOilDesert()
        );
    }
}
