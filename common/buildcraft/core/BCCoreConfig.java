/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import buildcraft.api.BCModules;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.BCLibConfig.ChunkLoaderLevel;
import buildcraft.lib.BCLibConfig.RenderRotation;
import buildcraft.lib.BCLibConfig.TimeGap;
import buildcraft.lib.config.*;
import buildcraft.lib.misc.ConfigUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.registry.RegistryConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

public class BCCoreConfig {
    // private static final List<Consumer<EnumRestartRequirement>> reloadListeners = new ArrayList<>();

    public static final Configuration config;
    public static Configuration objConfig;
    public static FileConfigManager detailedConfigManager;

    // Calen 1.20.1: use datagen
    public static boolean worldGen;
    public static boolean worldGenWaterSpring;
    public static boolean minePlayerProtected;
    public static boolean hidePower;
    public static boolean hideFluid;
    public static boolean pumpsConsumeWater;
    public static int markerMaxDistance;
    public static int pumpMaxDistance;
    public static int networkUpdateRate = 10;
    public static double miningMultiplier = 1;
    public static int miningMaxDepth;

    private static ConfigCategory<Boolean> propColourBlindMode;
    private static ConfigCategory<Boolean> propWorldGen;
    private static ConfigCategory<Boolean> propWorldGenWaterSpring;
    private static ConfigCategory<Boolean> propMinePlayerProtected;
    private static ConfigCategory<Boolean> propUseColouredLabels;
    private static ConfigCategory<Boolean> propUseHighContrastColouredLabels;
    private static ConfigCategory<Boolean> propHidePower;
    private static ConfigCategory<Boolean> propHideFluid;
    private static ConfigCategory<Boolean> propGuideBookEnableDetail;
    private static ConfigCategory<Integer> propGuideItemSearchLimit;
    private static ConfigCategory<Boolean> propUseBucketsStatic;
    private static ConfigCategory<Boolean> propUseBucketsFlow;
    private static ConfigCategory<Boolean> propUseLongLocalizedName;
    private static ConfigCategory<TimeGap> propDisplayTimeGap;
    private static ConfigCategory<Boolean> propUseSwappableSprites;
    private static ConfigCategory<Boolean> propEnableAnimatedSprites;
    private static ConfigCategory<Integer> propMaxGuideSearchResults;
    private static ConfigCategory<ChunkLoaderLevel> propChunkLoadLevel;
    private static ConfigCategory<RenderRotation> propItemRenderRotation;
    private static ConfigCategory<Integer> propItemLifespan;
    private static ConfigCategory<Boolean> propPumpsConsumeWater;
    private static ConfigCategory<Integer> propMarkerMaxDistance;
    private static ConfigCategory<Integer> propPumpMaxDistance;
    private static ConfigCategory<Integer> propNetworkUpdateRate;
    private static ConfigCategory<Double> propMiningMultiplier;
    private static ConfigCategory<Integer> propMiningMaxDepth;

    // Calen: just ensure <cinit> run and registered to RegistryConfig#modObjectConfigs
    public static synchronized void cinit() {
    }

    static {
        BCModules module = BCModules.CORE;
        config = new Configuration(module);

        File forgeConfigFolder = FMLPaths.CONFIGDIR.get().toFile();
        File buildCraftConfigFolder = new File(forgeConfigFolder, "buildcraft");

//        config = new Configuration(new File(buildCraftConfigFolder, "main.cfg"));
        objConfig = RegistryConfig.setRegistryConfig(BCCore.MODID, "objects");
        // Calen: thread safety
//        BCLibConfig.guiConfigFile = new File(buildCraftConfigFolder, "gui.json");
        BCLibConfig.getGuiConfigFileAndEnsureCreated();

        detailedConfigManager = new FileConfigManager(
                " The buildcraft detailed configuration file. This contains a lot of miscellaneous options that have no "
                        + "affect on gameplay.\n You should refer to the BC source code for a detailed description of what these do. (https://github.com/BuildCraft/BuildCraft)\n"
                        + " This file will be overwritten every time that buildcraf starts, so don't change anything other than the values.");
        detailedConfigManager.setConfigFile(new File(buildCraftConfigFolder, "detailed.properties"));

        createProps();

//        reloadConfig(EnumRestartRequirement.GAME);
        reloadConfig();
//        addReloadListener(BCCoreConfig::reloadConfig);
//        MinecraftForge.EVENT_BUS.register(BCCoreConfig.class);
        BCConfig.registerReloadListener(module, BCCoreConfig::reloadConfig);
    }

    public static void createProps() {
        // Variables to make
        String general = "general";
        String display = "display";
        String worldgen = "worldgen";
        String performance = "performance";

        EnumRestartRequirement none = EnumRestartRequirement.NONE;
        EnumRestartRequirement world = EnumRestartRequirement.WORLD;
//        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propColourBlindMode = config
                .define(display,
                        "Should I enable colorblind mode?",
                        none,
                        "colorBlindMode", false);

        propUseColouredLabels = config
                .define(display,
                        "Should colours be displayed as their own (or a similar) colour in tooltips?",
                        none,
                        "useColouredLabels", true);

        propUseHighContrastColouredLabels = config
                .define(display,
                        "Should colours displayed in tooltips use higher-contrast colours?",
                        none,
                        "useHighContrastColouredLabels", false);

        propHidePower = config
                .define(display,
                        "Should all power values (MJ, MJ/t) be hidden?",
                        none,
                        "hidePowerValues", false);

        propHideFluid = config
                .define(display,
                        "Should all fluid values (Buckets, mB, mB/t) be hidden?",
                        none,
                        "hideFluidValues", false);

        propGuideBookEnableDetail = config
                .define(display,
                        "",
                        none,
                        "guideBookEnableDetail", false);

        propUseBucketsStatic = config
                .define(display,
                        "Should static fluid values be displayed in terms of buckets rather than thousandths of a bucket? (B vs mB)",
                        none,
                        "useBucketsStatic", true);

        propUseBucketsFlow = config
                .define(display,
                        "Should flowing fluid values be displayed in terms of buckets per second rather than thousandths of a bucket per tick? (B/s vs mB/t)",
                        none,
                        "useBucketsFlow", true);

        propUseLongLocalizedName = config
                .define(display,
                        "Should localised strings be displayed in long or short form (10 mB / t vs 10 milli buckets per tick",
                        none,
                        "useLongLocalizedName", true);

        propDisplayTimeGap = config
                .defineEnum(display,
                        "Should localised strings be displayed in terms of seconds (1 MJ/s) or ticks (20 MJ/t)",
                        none,
                        "timeGap", TimeGap.SECONDS);

        propUseSwappableSprites = config
                .define(display,
                        "Disable this if you get texture errors with optifine. Disables some texture switching functionality "
                                + "when changing config options such as colour blind mode.",
                        world,
                        "useSwappableSprites", true);

        propItemRenderRotation = config
                .defineEnum(display,
                        "The rotation that items use when travelling through pipes. Set to 'enabled' for full rotation, "
                                + "'disabled' for no rotation, or 'horizontals_only' to only rotate items when going horizontally.",
                        none,
                        "itemRenderRotation", RenderRotation.ENABLED);

        propWorldGen = config
                .define(worldgen,
                        "Should BuildCraft generate anything in the world?",
                        world,
                        "enable", true);

        propWorldGenWaterSpring = config
                .define(worldgen,
                        "Should BuildCraft generate water springs?",
                        world,
                        "generateWaterSprings", true);

        propGuideItemSearchLimit = config
                .defineInRange(performance,
                        "The maximum number of items that the guide book will index.",
                        none,
                        "guideItemSearchLimit", 10_000, 1_500, 5_000_000);

        propEnableAnimatedSprites = config
                .define(performance,
                        "Disable this if you get sub-standard framerates due to buildcraftcore's ~60 sprites animating every frame.",
                        none,
                        "enableAnimatedSprites", true);

        propMaxGuideSearchResults = config
                .defineInRange(performance,
                        "The maximum number of search results to display in the guide book.",
                        none,
                        "maxGuideSearchResults", 1200, 500, 5000);

        propMinePlayerProtected = config
                .define(general,
                        "Should BuildCraft miners be allowed to break blocks using player-specific protection?",
                        none,
                        "miningBreaksPlayerProtectedBlocks", false);

        propChunkLoadLevel = config
                .defineEnum(general,
                        "",
                        world,
                        "chunkLoadLevel", ChunkLoaderLevel.SELF_TILES);

        propItemLifespan = config
                .defineInRange(general,
                        "How long, in seconds, should items stay on the ground? (Vanilla = 300, default = 60)",
                        none,
                        "itemLifespan", 60, 5, 600);

        propPumpsConsumeWater = config
                .define(general,
                        "Should pumps consume water? Enabling this will disable"
                                + " minor optimisations, but work properly with finite water mods.",
                        none,
                        "pumpsConsumeWater", false);

        propMarkerMaxDistance = config
                .defineInRange(general,
                        "How far, in minecraft blocks, should markers (volume and path) reach?",
                        none,
                        "markerMaxDistance", 64, 16, 256);

        propPumpMaxDistance = config
                .defineInRange(general,
                        "How far, in minecraft blocks, should pumps reach in fluids?",
                        none,
                        "pumpMaxDistance", 64, 16, 128);

        propNetworkUpdateRate = config
                .defineInRange(general,
                        "How often, in ticks, should network update packets be sent? Increasing this might help network performance.",
                        none,
                        "updateFactor", networkUpdateRate, 1, 100);

        propMiningMultiplier = config
                .defineInRange(general,
                        "How much power should be required for all mining machines?",
                        none,
                        "miningMultiplier", 1.0, 1, 200);

        propMiningMaxDepth = config
                .defineInRange(general,
                        "How much further down can miners (like the quarry or the mining well) dig?"
                                + "\n(Note: values above 256 only have an effect if a mod like cubic chunks is installed).",
                        none,
                        "miningMaxDepth", 512, 32, 4096);
    }

//    public static void addReloadListener(Consumer<EnumRestartRequirement> listener) {
//        reloadListeners.add(listener);
//    }

//    @SubscribeEvent
//    public static void onConfigChange(OnConfigChangedEvent cce) {
//        if (BCModules.isBcMod(cce.getModID())) {
//            EnumRestartRequirement req = EnumRestartRequirement.NONE;
//            if (Loader.instance().isInState(LoaderState.AVAILABLE)) {
//                // The loaders state will be LoaderState.SERVER_STARTED when we are in a world
//                req = EnumRestartRequirement.WORLD;
//            }
//            for (Consumer<EnumRestartRequirement> listener : reloadListeners) {
//                listener.accept(req);
//            }
//        }
//    }

    public static void postInit() {
        ConfigUtil.setLang(config);
//        saveConfigs();
        saveCoreConfigs();
        saveObjConfigs();
    }

    //    public static void saveConfigs() {
//        if (config.hasChanged()) {
//            config.save();
//        }
//        if (objConfig.hasChanged()) {
//            objConfig.save();
//        }
//    }
    public static void saveCoreConfigs() {
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void saveObjConfigs() {
        if (objConfig.hasChanged()) {
            objConfig.save();
        }
    }

    // public static void reloadConfig(EnumRestartRequirement restarted)
    public static void reloadConfig() {
        minePlayerProtected = propMinePlayerProtected.get();
        BCLibConfig.useColouredLabels = propUseColouredLabels.get();
        BCLibConfig.useHighContrastLabelColours = propUseHighContrastColouredLabels.get();
        hidePower = propHidePower.get();
        hideFluid = propHideFluid.get();
        BCLibConfig.guideShowDetail = propGuideBookEnableDetail.get();
        BCLibConfig.guideItemSearchLimit = MathUtil.clamp(propGuideItemSearchLimit.get(), 1_500, 5_000_000);
        BCLibConfig.useBucketsStatic = propUseBucketsStatic.get();
        BCLibConfig.useBucketsFlow = propUseBucketsFlow.get();
        BCLibConfig.useLongLocalizedName = propUseLongLocalizedName.get();
        BCLibConfig.itemLifespan = propItemLifespan.get();
        pumpsConsumeWater = propPumpsConsumeWater.get();
        markerMaxDistance = propMarkerMaxDistance.get();
        pumpMaxDistance = propPumpMaxDistance.get();
        BCLibConfig.colourBlindMode = propColourBlindMode.get();
        BCLibConfig.displayTimeGap = propDisplayTimeGap.get();
        BCLibConfig.rotateTravelingItems = propItemRenderRotation.get();
        BCLibConfig.enableAnimatedSprites = propEnableAnimatedSprites.get();
        miningMultiplier = MathUtil.clamp(propMiningMultiplier.get(), 1, 200);
        miningMaxDepth = propMiningMaxDepth.get();

//        if (EnumRestartRequirement.WORLD.hasBeenRestarted(restarted)) {
        BCLibConfig.chunkLoadingLevel = propChunkLoadLevel.get();

//            if (EnumRestartRequirement.GAME.hasBeenRestarted(restarted)) {
        worldGen = propWorldGen.get();
        worldGenWaterSpring = propWorldGenWaterSpring.get();
        BCLibConfig.useSwappableSprites = propUseSwappableSprites.get();
//            }
//        }
        BCLibConfig.refreshConfigs();
//        saveConfigs();
        saveCoreConfigs();
        saveObjConfigs();
    }
}
