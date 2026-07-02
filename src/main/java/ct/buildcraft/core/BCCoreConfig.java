/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package ct.buildcraft.core;



import ct.buildcraft.lib.BCLibConfig;
import ct.buildcraft.lib.BCLibConfig.ChunkLoaderLevel;
import ct.buildcraft.lib.BCLibConfig.RenderRotation;
import ct.buildcraft.lib.BCLibConfig.TimeGap;

import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;


public class BCCoreConfig {


    public static ForgeConfigSpec config;
    
    public static boolean worldGen;
    public static boolean worldGenWaterSpring;
    public static boolean minePlayerProtected;
    public static boolean hidePower;
    public static boolean hideFluid;
    public static boolean pumpsConsumeWater;
    public static int markerMaxDistance;
    public static int pumpMaxDistance;
    public static int networkUpdateRate;//10
    public static double miningMultiplier;//1
    public static int miningMaxDepth;

    private static BooleanValue propColourBlindMode;
    private static BooleanValue propUseColouredLabels;
    private static BooleanValue propUseHighContrastColouredLabels;
    private static BooleanValue propHidePower;
    private static BooleanValue propHideFluid;
    private static BooleanValue propGuideBookEnableDetail;
    private static BooleanValue propUseBucketsStatic;
    private static BooleanValue propUseBucketsFlow;
    private static BooleanValue propUseLongLocalizedName;
    private static BooleanValue propUseSwappableSprites;
    private static BooleanValue propWorldGen;
    private static BooleanValue propWorldGenWaterSpring;
    private static BooleanValue propMinePlayerProtected;

    private static IntValue propGuideItemSearchLimit;

    private static EnumValue<TimeGap> propDisplayTimeGap;

    private static BooleanValue propEnableAnimatedSprites;
    private static IntValue propMaxGuideSearchResults;
    private static EnumValue<ChunkLoaderLevel> propChunkLoadLevel;
    private static EnumValue<RenderRotation> propItemRenderRotation;
    private static IntValue propItemLifespan;
    private static BooleanValue propPumpsConsumeWater;
    private static IntValue propMarkerMaxDistance;
    private static IntValue propPumpMaxDistance;
    private static IntValue propNetworkUpdateRate;
    private static DoubleValue propMiningMultiplier;
    private static IntValue propMiningMaxDepth;

    public static void registry() {
        ForgeConfigSpec.Builder con_builder = new ForgeConfigSpec.Builder();

/*        config = new ForgeConfigSpec(new File(cfgFolder, "main.cfg"));
        objConfig = RegistryConfig.setRegistryConfig(BCCore.MODID, new File(cfgFolder, "objects.cfg"));
        BCLibConfig.guiConfigFile = new File(cfgFolder, "gui.json");

        detailedConfigManager = new FileConfigManager(
            " The buildcraft detailed configuration file. This contains a lot of miscellaneous options that have no "
                + "affect on gameplay.\n You should refer to the BC source code for a detailed description of what these do. (https://github.com/BuildCraft/BuildCraft)\n"
                + " This file will be overwritten every time that buildcraft starts, so don't change anything other than the values.");
        detailedConfigManager.setConfigFile(new File(cfgFolder, "detailed.properties"));

        // Variables to make
        String general = Configuration.CATEGORY_GENERAL;*/

        con_builder.push("display");
        propColourBlindMode = con_builder.comment("Should I enable colorblind mode?")
        .define("colorBlindMode", false);
        
        propUseColouredLabels = con_builder.comment("Should colours be displayed as their own (or a similar) colour in tooltips?")
        .define("useColouredLabels", true);
        
        propUseHighContrastColouredLabels = con_builder.comment("Should colours displayed in tooltips use higher-contrast colours?")
        .define("useColouredLabels", false);
        
        propHidePower = con_builder.comment("Should all power values (MJ, MJ/t) be hidden?")
        .define("hidePowerValues", false);
        
        propHideFluid = con_builder.comment("Should all fluid values (Buckets, mB, mB/t) be hidden?")
        .define("hideFluidValues", false);
        
        propGuideBookEnableDetail = con_builder
        .define("guideBookEnableDetail", false);

        propUseBucketsStatic = con_builder.comment("Should static fluid values be displayed in terms of buckets rather than thousandths of a bucket? (B vs mB)")
        .define("useBucketsStatic", true);
        
        propUseBucketsFlow = con_builder.comment("Should flowing fluid values be displayed in terms of buckets per second rather than thousandths of a bucket per tick? (B/s vs mB/t)")
        .define("useBucketsFlow", true);
        
        propUseLongLocalizedName = con_builder.comment("Should localised strings be displayed in long or short form (10 mB / t vs 10 milli buckets per tick")
        .define("useLongLocalizedName", true);;
        
        propDisplayTimeGap = con_builder.comment("Should localised strings be displayed in terms of seconds (20 MJ/s) or ticks (1 MJ/t)")
        .defineEnum("timeGap", TimeGap.SECONDS, TimeGap.values());
        
        propUseSwappableSprites = con_builder.comment("Disable this if you get texture errors with optifine. Disables some texture switching functionality"
        		+ "when changing config options such as colour blind mode.")
        .define("useSwappableSprites", true);
        
        propItemRenderRotation = con_builder.comment("The rotation that items use when travelling through pipes. Set to 'enabled' for full rotation, "
                + "'disabled' for no rotation, or 'horizontals_only' to only rotate items when going horizontally.")
        .defineEnum("itemRenderRotation", RenderRotation.ENABLED, RenderRotation.values());
        
        
        con_builder.pop();
        con_builder.push("worldgen");
        
        propWorldGen = con_builder.comment("Should BuildCraft generate anything in the world?")
        .worldRestart().define("enable", true);
        
        propWorldGenWaterSpring = con_builder.comment("Should BuildCraft generate water springs?")
        .worldRestart().define("generateWaterSprings", true);

        con_builder.pop();
        con_builder.push("general");
        
        propMinePlayerProtected = con_builder.comment("Should BuildCraft miners be allowed to break blocks using player-specific protection?")
        .define("miningBreaksPlayerProtectedBlocks", false);

        propChunkLoadLevel = con_builder.comment("").worldRestart()
        .defineEnum("chunkLoadLevel", ChunkLoaderLevel.SELF_TILES,ChunkLoaderLevel.values());

        propItemLifespan = con_builder.comment("How long, in seconds, should items stay on the ground? (Vanilla = 300, default = 60)")
        .defineInRange("itemLifespan", 60, 5, 600);

        propPumpsConsumeWater = con_builder.comment("Should pumps consume water? Enabling this will disable"
                + " minor optimisations, but work properly with finite water mods.")
        .define("pumpsConsumeWater", false);
        
        propMarkerMaxDistance = con_builder.comment("How far, in minecraft blocks, should markers (volume and path) reach?")
        .defineInRange("markerMaxDistance", 64, 16, 256);

        propPumpMaxDistance = con_builder.comment("How far, in minecraft blocks, should pumps reach in fluids? (Default: 48, matching the reduced circular search radius)")
        .defineInRange("pumpMaxDistance", 64, 16, 128);

        propNetworkUpdateRate = con_builder.comment("How often, in ticks, should network update packets be sent? Increasing this might help network performance.")
        .defineInRange("updateFactor", networkUpdateRate, 1, 10);

        propMiningMultiplier = con_builder.comment("How much power should be required for all mining machines?")
        .defineInRange("miningMultiplier", 1.0, 1, 200);
        
        propMiningMaxDepth = con_builder.comment("How much further down can miners (like the quarry or the mining well) dig?"
                + "\n(Note: values above 256 only have an effect if a mod like cubic chunks is installed).")
        .defineInRange("miningMaxDepth", 256, 32, 4096);

        propEnableAnimatedSprites = con_builder.comment("Disable this if you get sub-standard framerates due to buildcraft's ~60 sprites animating every frame.")
        .define("enableAnimatedSprites", true);
        
        propMaxGuideSearchResults = con_builder.comment("The maximum number of search results to display in the guide book.")
        .defineInRange("maxGuideSearchResults", 1200, 500, 5000);
        
        propGuideItemSearchLimit = con_builder.comment("The maximum number of items that the guide book will index.")
        .defineInRange("guideItemSearchLimit", 10_000, 1_500, 5_000_000);

        config = con_builder.build();

        MinecraftForge.EVENT_BUS.register(BCCoreConfig.class);
    }

    @SubscribeEvent
    public static void onReloadConfig(final ModConfigEvent.Reloading restarted) {
    	reloadConfig(restarted.getConfig().getModId());
    }
    
    @SubscribeEvent
    public static void onLoadConfig(final ModConfigEvent.Loading load) {
    	reloadConfig(load.getConfig().getModId());
    }

    protected static void reloadConfig(String modid) {
    	if(!modid.equals(BCCore.MODID))return ;
        minePlayerProtected = propMinePlayerProtected.get();
        BCLibConfig.useColouredLabels = propUseColouredLabels.get();
        BCLibConfig.useHighContrastLabelColours = propUseHighContrastColouredLabels.get();
        hidePower = propHidePower.get();
        hideFluid = propHideFluid.get();
        BCLibConfig.guideShowDetail = propGuideBookEnableDetail.get();
        BCLibConfig.guideItemSearchLimit = Mth.clamp(propGuideItemSearchLimit.get(), 1_500, 5_000_000);
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
        miningMultiplier = Mth.clamp(propMiningMultiplier.get(), 1, 200);
        miningMaxDepth = propMiningMaxDepth.get();
        
        
        BCLibConfig.chunkLoadingLevel = propChunkLoadLevel.get();

        worldGen = propWorldGen.get();
        worldGenWaterSpring = propWorldGenWaterSpring.get();
        BCLibConfig.useSwappableSprites = propUseSwappableSprites.get();
        
        BCLibConfig.refreshConfigs();
    }
}
