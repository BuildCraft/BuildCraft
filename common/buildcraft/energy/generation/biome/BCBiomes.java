package buildcraft.energy.generation.biome;

import buildcraft.energy.BCEnergy;
import net.minecraft.client.audio.BackgroundMusicSelector;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMaker;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

// Calen: OverworldBiomes.class https://forums.minecraftforge.net/topic/104296-solved1165-custom-biome-not-generating-in-overworld-dimension/
@Mod.EventBusSubscriber(modid = BCEnergy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCBiomes {
    @Nullable
    private static final BackgroundMusicSelector NORMAL_MUSIC = null;

//    // Calen test
//    public static final DeferredRegister<Biome> reg = DeferredRegister.create(ForgeRegistries.BIOMES, BCEnergy.MODID);
//    //    public static final RegistryObject<Biome> OIL_DESERT = reg.register(BCBiomeRegistry.BIOME_OIL_DESERT, BCBiomes::makeOilDesertBiome);
////    public static final RegistryObject<Biome> OIL_OCEAN = reg.register(BCBiomeRegistry.BIOME_OIL_OCEAN, BCBiomes::makeOilOceanBiome);
//    public static RegistryObject<Biome> OIL_DESERT;
//    public static RegistryObject<Biome> OIL_OCEAN;
//
//    public static void init() {
//        OIL_DESERT = reg.register(BCBiomeRegistry.BIOME_OIL_DESERT, BCBiomes::makeOilDesertBiome);
//        OIL_OCEAN = reg.register(BCBiomeRegistry.BIOME_OIL_OCEAN, BCBiomes::makeOilOceanBiome);
//        reg.register(FMLJavaModLoadingContext.get().getModEventBus());
//    }

    @SubscribeEvent
    public static void initBiome(RegistryEvent.Register<Biome> event) {
        Biome oil_desert = makeOilDesertBiome().setRegistryName(BCEnergy.MODID, BCBiomeRegistry.BIOME_OIL_DESERT);
        Biome oil_ocean = makeOilOceanBiome().setRegistryName(BCEnergy.MODID, BCBiomeRegistry.BIOME_OIL_OCEAN);
////        event.getRegistry().register();
////        event.getRegistry().register();
////        BiomeManager.addAdditionalOverworldBiomes(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_DESERT);
////        BiomeManager.addAdditionalOverworldBiomes(BCBiomeRegistry.RESOURCE_KEY_BIOME_OIL_OCEAN);
//
        ForgeRegistries.BIOMES.register(oil_desert);
        ForgeRegistries.BIOMES.register(oil_ocean);
    }

    // Calen: the same as Desert
    public static Biome makeOilDesertBiome() {
        return BiomeMaker.desertBiome(0.125F, 0.05F, true, true, true);

    }

    // Calen: the same as Ocean
    public static Biome makeOilOceanBiome() {
        return BiomeMaker.oceanBiome(false);
    }
}
