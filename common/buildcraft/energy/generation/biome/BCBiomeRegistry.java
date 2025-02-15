package buildcraft.energy.generation.biome;

import buildcraft.energy.BCEnergy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BCBiomeRegistry {
    public static String BIOME_OIL_OCEAN = "oil_ocean";
    public static String BIOME_OIL_DESERT = "oil_desert";

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, BCEnergy.MODID);

//    public static final RegistryObject<Biome> OIL_DESERT = BIOMES.register(BCWorldGenNames.BIOME_OIL_DESERT, () -> BCBiomes.makeOilDesertBiome());
//    public static final RegistryObject<Biome> OIL_OCEAN = BIOMES.register(BCWorldGenNames.BIOME_OIL_OCEAN, () -> BCBiomes.makeOilOceanBiome());

    public static final ResourceLocation RL_BIOME_OIL_DESERT = new ResourceLocation(BCEnergy.MODID, BIOME_OIL_DESERT);
    public static final ResourceLocation RL_BIOME_OIL_OCEAN = new ResourceLocation(BCEnergy.MODID, BIOME_OIL_OCEAN);
    public static final ResourceKey<Biome> RESOURCE_KEY_BIOME_OIL_DESERT = ResourceKey.create(Registries.BIOME, RL_BIOME_OIL_DESERT);
    public static final ResourceKey<Biome> RESOURCE_KEY_BIOME_OIL_OCEAN = ResourceKey.create(Registries.BIOME, RL_BIOME_OIL_OCEAN);


//    public static void addBiomesToOverworld() {
//        BiomeManager.addBiome(BiomeManager.BiomeType.DESERT, new BiomeManager.BiomeEntry(RESOURCE_KEY_BIOME_OIL_DESERT, 10));
//        BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(RESOURCE_KEY_BIOME_OIL_OCEAN, 10));
//    }

    // static
    public static void init() {
//        boolean log = OilGenerator.DEBUG_OILGEN_BASIC;
//        // 海洋油田
//        if (BCEnergyConfig.enableOilOceanBiome) {
//            BiomeManager.addBiome(BiomeManager.BiomeType.COOL, new BiomeManager.BiomeEntry(RESOURCE_KEY_BIOME_OIL_OCEAN, 10));
////            BiomeDictionary.addTypes(
////                    RESOURCE_KEY_BIOME_OIL_OCEAN,
////                    BiomeDictionary.Type.OCEAN
////            );
////            OverworldBiomeBuilder.MIDDLE_BIOMES[4][4] = RESOURCE_KEY_BIOME_OIL_DESERT;
//            BCLog.logger.info("[energy.oilgen] Registered the ocean oil biome.");
//        } else {
//            BCLog.logger.info("[energy.oilgen] Not registering the ocean oil biome, as it has been disabled by the config file.");
//        }
//        // 沙漠油田
//        if (BCEnergyConfig.enableOilDesertBiome) {
//            BiomeManager.addBiome(BiomeManager.BiomeType.DESERT, new BiomeManager.BiomeEntry(RESOURCE_KEY_BIOME_OIL_DESERT, 10));
////            // TODO Calen reg biome???
////            // Calen test
////            BuiltinRegistries.register(BuiltinRegistries.BIOME, RESOURCE_KEY_BIOME_OIL_DESERT, BCBiomes.makeOilDesertBiome());
//
////            BiomeDictionary.addTypes(
////                    RESOURCE_KEY_BIOME_OIL_DESERT,
////                    BiomeDictionary.Type.HOT,
////                    BiomeDictionary.Type.DRY,
////                    BiomeDictionary.Type.SANDY
////            );
//            BCLog.logger.info("[energy.oilgen] Registered the desert oil biome.");
//        } else {
//            BCLog.logger.info("[energy.oilgen] Not registering the desert oil biome, as it has been disabled by the config file.");
//        }
//        if (BCCoreConfig.worldGen) {
//            if (BCEnergyConfig.enableOilGeneration) {
//                // TODO Calen
////                MinecraftForge.EVENT_BUS.register(OilStructureGenerator.class);
//                BCLog.logger.info("[energy.oilgen] Registered the oil spout generator");
//            } else {
//                BCLog.logger.info("[energy.oilgen] Not registering the oil spout generator, as it has been disabled by the config file.");
//            }
//            if (BCEnergyConfig.enableOilOceanBiome || BCEnergyConfig.enableOilDesertBiome) {
//                // TODO Calen
////                MinecraftForge.TERRAIN_GEN_BUS.register(new BiomeInitializer());
//                BCLog.logger.info("[energy.oilgen] Registered the oil biome initiializer");
//            } else {
//                BCLog.logger.info("[energy.oilgen] Not registering the oil biome initiializer, as it has been disabled by the config file.");
//            }
//        } else {
//            BCLog.logger.info("[energy.oilgen] Not registering any world-gen, as everything has been disabled by the config file.");
//        }
    }
}
