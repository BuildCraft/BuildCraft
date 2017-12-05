/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraft.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import buildcraft.api.BCModules;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.BCLibConfig.TimeGap;
import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.config.FileConfigManager;
import buildcraft.lib.misc.ConfigUtil;
import buildcraft.lib.registry.RegistryConfig;

public class BCCoreConfig {
    private static final List<Consumer<EnumRestartRequirement>> reloadListeners = new ArrayList<>();

    public static Configuration config;
    public static Configuration objConfig;
    public static FileConfigManager detailedConfigManager;

    public static boolean worldGen;
    public static boolean worldGenWaterSpring;
    public static boolean minePlayerProtected;
    public static boolean hidePower;
    public static boolean hideFluid;
    public static boolean pumpsConsumeWater;
    public static int markerMaxDistance;
    public static int networkUpdateRate = 10;

    private static Property propColourBlindMode;
    private static Property propWorldGen;
    private static Property propWorldGenWaterSpring;
    private static Property propMinePlayerProtected;
    private static Property propUseColouredLabels;
    private static Property propUseHighContrastColouredLabels;
    private static Property propHidePower;
    private static Property propHideFluid;
    private static Property propUseBucketsStatic;
    private static Property propUseBucketsFlow;
    private static Property propUseLongLocalizedName;
    private static Property propDisplayTimeGap;
    private static Property propUseSwappableSprites;
    private static Property propItemLifespan;
    private static Property propPumpsConsumeWater;
    private static Property propMarkerMaxDistance;
    private static Property propNetworkUpdateRate;

    public static void preInit(File cfgFolder) {
        config = new Configuration(new File(cfgFolder, "main.cfg"));
        objConfig = RegistryConfig.setRegistryConfig(BCCore.MODID, new File(cfgFolder, "objects.cfg"));
        BCLibConfig.guiConfigFile = new File(cfgFolder, "gui.json");

        detailedConfigManager = new FileConfigManager(
            " The buildcraft detailed configuration file. This contains a lot of miscellaneous options that have no "
                + "affect on gameplay.\n You should refer to the BC source code for a detailed description of what these do. (https://github.com/BuildCraft/BuildCraft)\n"
                + " This file will be overwritten every time that buildcraft starts, so don't change anything other than the values.");
        detailedConfigManager.setConfigFile(new File(cfgFolder, "detailed.properties"));

        // Variables to make
        String general = Configuration.CATEGORY_GENERAL;
        String display = "display";
        String worldgen = "worldgen";

        EnumRestartRequirement none = EnumRestartRequirement.NONE;
        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propColourBlindMode = config.get(display, "colorBlindMode", false);
        propColourBlindMode.setComment("Should I enable colorblind mode?");
        none.setTo(propColourBlindMode);

        propWorldGen = config.get(worldgen, "enable", true);
        propWorldGen.setComment("Should BuildCraft generate anything in the world?");
        game.setTo(propWorldGen);

        propWorldGenWaterSpring = config.get(worldgen, "generateWaterSprings", true);
        propWorldGenWaterSpring.setComment("Should BuildCraft generate water springs?");
        game.setTo(propWorldGenWaterSpring);

        propMinePlayerProtected = config.get(general, "miningBreaksPlayerProtectedBlocks", false);
        propMinePlayerProtected
            .setComment("Should BuildCraft miners be allowed to break blocks using player-specific protection?");
        none.setTo(propMinePlayerProtected);

        propUseColouredLabels = config.get(display, "useColouredLabels", true);
        propUseColouredLabels.setComment("Should colours be displayed as their own (or a similar) colour in tooltips?");
        none.setTo(propUseColouredLabels);

        propUseHighContrastColouredLabels = config.get(display, "useHighContrastColouredLabels", false);
        propUseHighContrastColouredLabels
            .setComment("Should colours displayed in tooltips use higher-contrast colours?");
        none.setTo(propUseHighContrastColouredLabels);

        propHidePower = config.get(display, "hidePowerValues", false);
        propHidePower.setComment("Should all power values (MJ, MJ/t) be hidden?");
        none.setTo(propHidePower);

        propHideFluid = config.get(display, "hideFluidValues", false);
        propHideFluid.setComment("Should all fluid values (Buckets, mB, mB/t) be hidden?");
        none.setTo(propHideFluid);

        propUseBucketsStatic = config.get(display, "useBucketsStatic", false);
        propUseBucketsStatic.setComment(
            "Should static fluid values be displayed in terms of buckets rather than thousandths of a bucket? (B vs mB)");
        none.setTo(propUseBucketsStatic);

        propUseBucketsFlow = config.get(display, "useBucketsFlow", false);
        propUseBucketsFlow.setComment(
            "Should flowing fluid values be displayed in terms of buckets per second rather than thousandths of a bucket per tick? (B/s vs mB/t)");
        none.setTo(propUseBucketsFlow);

        propUseLongLocalizedName = config.get(display, "useLongLocalizedName", false);
        propUseLongLocalizedName.setComment(
            "Should localised strings be displayed in long or short form (10 mB / t vs 10 milli buckets per tick");
        none.setTo(propUseLongLocalizedName);

        propDisplayTimeGap = config.get(display, "timeGap", TimeGap.TICKS.name().toLowerCase(Locale.ROOT));
        propDisplayTimeGap
            .setComment("Should localised strings be displayed in terms of seconds (1 MJ/s) or ticks (20 MJ/t)");
        ConfigUtil.setEnumProperty(propDisplayTimeGap, TimeGap.values());
        none.setTo(propDisplayTimeGap);

        propUseSwappableSprites = config.get(display, "useSwappableSprites", true);
        propUseSwappableSprites.setComment(
            "Disable this if you get texture errors with optifine. Disables some texture switching functionality "
                + "when changing config options such as colour blind mode.");
        game.setTo(propUseSwappableSprites);

        propItemLifespan = config.get(general, "itemLifespan", 60);
        propItemLifespan.setMinValue(5).setMaxValue(600);
        propItemLifespan
            .setComment("How long, in seconds, should items stay on the ground? (Vanilla = 300, default = 60)");
        none.setTo(propItemLifespan);

        propPumpsConsumeWater = config.get(general, "pumpsConsumeWater", false);
        propPumpsConsumeWater.setComment("Should pumps consume water? Enabling this will disable"
            + " minor optimisations, but work properly with finite water mods.");
        none.setTo(propPumpsConsumeWater);

        propMarkerMaxDistance = config.get(general, "markerMaxDistance", 64);
        propMarkerMaxDistance.setMinValue(16).setMaxValue(256);
        propMarkerMaxDistance.setComment("How far, in minecraft blocks, should markers (volume and path) reach?");
        none.setTo(propMarkerMaxDistance);

        propNetworkUpdateRate = config.get(general, "updateFactor", networkUpdateRate);
        propNetworkUpdateRate.setMinValue(1).setMaxValue(100);
        propNetworkUpdateRate.setComment(
            "How often, in ticks, should network update packets be sent? Increasing this might help network performance.");
        none.setTo(propNetworkUpdateRate);

        reloadConfig(game);
        addReloadListener(BCCoreConfig::reloadConfig);

        MinecraftForge.EVENT_BUS.register(BCCoreConfig.class);
    }

    public static void addReloadListener(Consumer<EnumRestartRequirement> listener) {
        reloadListeners.add(listener);
    }

    @SubscribeEvent
    public static void onConfigChange(OnConfigChangedEvent cce) {
        if (BCModules.isBcMod(cce.getModID())) {
            EnumRestartRequirement req = EnumRestartRequirement.NONE;
            if (Loader.instance().isInState(LoaderState.AVAILABLE)) {
                // The loaders state will be LoaderState.SERVER_STARTED when we are in a world
                req = EnumRestartRequirement.WORLD;
            }
            for (Consumer<EnumRestartRequirement> listener : reloadListeners) {
                listener.accept(req);
            }
        }
    }

    public static void postInit() {
        ConfigUtil.setLang(config);
        if (config.hasChanged()) {
            config.save();
        }
        if (objConfig.hasChanged()) {
            objConfig.save();
        }
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        minePlayerProtected = propMinePlayerProtected.getBoolean();
        BCLibConfig.useColouredLabels = propUseColouredLabels.getBoolean();
        BCLibConfig.useHighContrastLabelColours = propUseHighContrastColouredLabels.getBoolean();
        hidePower = propHidePower.getBoolean();
        hideFluid = propHideFluid.getBoolean();
        BCLibConfig.useBucketsStatic = propUseBucketsStatic.getBoolean();
        BCLibConfig.useBucketsFlow = propUseBucketsFlow.getBoolean();
        BCLibConfig.useLongLocalizedName = propUseLongLocalizedName.getBoolean();
        BCLibConfig.itemLifespan = propItemLifespan.getInt();
        pumpsConsumeWater = propPumpsConsumeWater.getBoolean();
        markerMaxDistance = propMarkerMaxDistance.getInt();
        BCLibConfig.colourBlindMode = propColourBlindMode.getBoolean();
        BCLibConfig.displayTimeGap =
            ConfigUtil.parseEnumForConfig(propDisplayTimeGap.getString(), TimeGap.values(), TimeGap.TICKS);

        if (EnumRestartRequirement.GAME.hasBeenRestarted(restarted)) {
            worldGen = propWorldGen.getBoolean();
            worldGenWaterSpring = propWorldGenWaterSpring.getBoolean();
        }
        BCLibConfig.refreshConfigs();
        if (config.hasChanged()) {
            config.save();
        }
        if (objConfig.hasChanged()) {
            objConfig.save();
        }
    }
}
