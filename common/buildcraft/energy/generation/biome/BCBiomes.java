package buildcraft.energy.generation.biome;

import buildcraft.energy.BCEnergy;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

// Calen: OverworldBiomes.class https://forums.minecraftforge.net/topic/104296-solved1165-custom-biome-not-generating-in-overworld-dimension/
@Mod.EventBusSubscriber(modid = BCEnergy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCBiomes {
    @Nullable
    private static final Music NORMAL_MUSIC = null;

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
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.desertSpawns(mobspawnsettings$builder);
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addFossilDecoration(biomegenerationsettings$builder);
        globalOverworldGeneration(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDesertVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDesertExtraVegetation(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDesertExtraDecoration(biomegenerationsettings$builder);

        return biome(
                Biome.Precipitation.NONE,
                Biome.BiomeCategory.DESERT,
                2.0F,
                0.0F,
                4159204,
                329011,
                mobspawnsettings$builder,
                biomegenerationsettings$builder,
                NORMAL_MUSIC
        );

    }

    // Calen: the same as Ocean
    public static Biome makeOilOceanBiome() {
        MobSpawnSettings.Builder mobspawnsettings$builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(mobspawnsettings$builder, 1, 4, 10);
        mobspawnsettings$builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
        BiomeGenerationSettings.Builder biomegenerationsettings$builder = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultOres(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addWaterTrees(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultFlowers(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultGrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultMushrooms(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(biomegenerationsettings$builder);
        biomegenerationsettings$builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_NORMAL);
        BiomeDefaultFeatures.addDefaultSeagrass(biomegenerationsettings$builder);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(biomegenerationsettings$builder);

        return biome(
                Biome.Precipitation.RAIN,
                Biome.BiomeCategory.OCEAN,
                0.5F,
                0.5F,
                4159204,
                329011,
                mobspawnsettings$builder,
                biomegenerationsettings$builder,
                NORMAL_MUSIC
        );
    }

    private static Biome biome(
            Biome.Precipitation p_194852_,
            Biome.BiomeCategory p_194853_,
            float temperature,
            float downfall,
            int waterColor,
            int waterFogColor,
            MobSpawnSettings.Builder mobSpawn,
            BiomeGenerationSettings.Builder settings,
            @Nullable Music music
    ) {
        return (new Biome.BiomeBuilder())
                .precipitation(p_194852_)
                .biomeCategory(p_194853_)
                .temperature(temperature)
                .downfall(downfall)
                .specialEffects(
                        (new BiomeSpecialEffects.Builder())
                                .waterColor(waterColor)
                                .waterFogColor(waterFogColor)
                                .fogColor(12638463)
                                .skyColor(calculateSkyColor(temperature))
                                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                                .backgroundMusic(music)
                                .build()
                )
                .mobSpawnSettings(mobSpawn.build())
                .generationSettings(settings.build())
                .build();
    }

    private static void globalOverworldGeneration(BiomeGenerationSettings.Builder p_194870_) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(p_194870_);
        BiomeDefaultFeatures.addDefaultCrystalFormations(p_194870_);
        BiomeDefaultFeatures.addDefaultMonsterRoom(p_194870_);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(p_194870_);
        BiomeDefaultFeatures.addDefaultSprings(p_194870_);
        BiomeDefaultFeatures.addSurfaceFreezing(p_194870_);
    }

    protected static int calculateSkyColor(float p_194844_) {
        float $$1 = p_194844_ / 3.0F;
        $$1 = Mth.clamp($$1, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F);
    }
}
