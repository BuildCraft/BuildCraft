package buildcraft.energy;

// TODO Calen: Biome
public class BCEnergyWorldGen {
    public static void preInit() {
//        boolean log = OilGenerator.DEBUG_OILGEN_BASIC;
        // 1.18.2: use datagen
//        if (BCEnergyConfig.enableOilOceanBiome) {
//            BiomeDictionary.addTypes(
//                    BiomeOilOcean.INSTANCE,
//                    BiomeDictionary.Type.OCEAN
//            );
//            BCLog.logger.info("[energy.oilgen] Registered the ocean oil biome.");
//        } else {
//            BCLog.logger.info("[energy.oilgen] Not registering the ocean oil biome, as it has been disabled by the config file.");
//        }
//        if (BCEnergyConfig.enableOilDesertBiome) {
//            BiomeDictionary.addTypes(
//                    BiomeOilDesert.INSTANCE,
//                    BiomeDictionary.Type.HOT,
//                    BiomeDictionary.Type.DRY,
//                    BiomeDictionary.Type.SANDY
//            );
//            BCLog.logger.info("[energy.oilgen] Registered the desert oil biome.");
//        } else {
//            BCLog.logger.info("[energy.oilgen] Not registering the desert oil biome, as it has been disabled by the config file.");
//        }
//        if (BCCoreConfig.worldGen) {
//            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
//            modEventBus.addGenericListener(StructureFeature.class, OilStructureRegistry::register);
//            if (BCEnergyConfig.enableOilOceanBiome || BCEnergyConfig.enableOilDesertBiome) {
//                MinecraftForge.EVENT_BUS.register(new BiomeInitializer());
//                BCLog.logger.info("[energy.oilgen] Registered the oil biome initiializer");
//            } else {
//                BCLog.logger.info("[energy.oilgen] Not registering the oil biome initiializer, as it has been disabled by the config file.");
//            }
//        } else {
//            BCLog.logger.info("[energy.oilgen] Not registering any world-gen, as everything has been disabled by the config file.");
//        }
    }

//    @SubscribeEvent
//    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
//        if (BCEnergyConfig.enableOilDesertBiome) {
//            event.getRegistry().register(new BiomeOilOcean());
//        }
//        if (BCEnergyConfig.enableOilDesertBiome) {
//            event.getRegistry().register(new BiomeOilDesert());
//        }
//    }
}
