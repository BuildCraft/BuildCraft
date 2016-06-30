/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import buildcraft.lib.config.EnumRestartRequirement;
import buildcraft.lib.config.FileConfigManager;

public class BCCoreConfig {
    public static Configuration config;
    public static FileConfigManager detailedConfigManager;

    public static boolean colourBlindMode;
    public static boolean worldGen;
    public static boolean worldGenWaterSpring;
    public static boolean useLocalServerOnClient;
    public static boolean minePlayerProteted;
    public static boolean hidePower;
    public static boolean hideFluid;
    public static boolean useBucketsStatic;
    public static boolean useBucketsFlow;
    public static int itemLifespan;
    public static int markerMaxDistance;

    private static Property propColourBlindMode;
    private static Property propWorldGen;
    private static Property propWorldGenWaterSpring;
    private static Property propUseLocalServerOnClient;
    private static Property propMinePlayerProtected;
    private static Property propHidePower;
    private static Property propHideFluid;
    private static Property propUseBucketsStatic;
    private static Property propUseBucketsFlow;
    private static Property propItemLifespan;
    private static Property propMarkerMaxDistance;

    public static void preInit(File cfgFile) {
        config = new Configuration(new File(cfgFile, "main.cfg"));

        detailedConfigManager = new FileConfigManager(" The buildcraft detailed configuration file. This contains a lot of miscelaneous options that have no "
            + "affect on gameplay.\n You should refer to the BC source code for a detailed description of what these do. (https://github.com/BuildCraft/BuildCraft)\n"
            + " This file will be overwritten every time that buildcraft starts, so there is no point in adding comments");
        detailedConfigManager.setConfigFile(new File(cfgFile, "detailed.properties"));

        // Variables to make
        String general = Configuration.CATEGORY_GENERAL;
        String display = "display";
        String worldgen = "worldgen";

        EnumRestartRequirement none = EnumRestartRequirement.NONE;
        EnumRestartRequirement game = EnumRestartRequirement.GAME;

        propColourBlindMode = config.get(display, "colorBlindMode", false);
        propColourBlindMode.setComment("Should I enable colorblind mode?");
        game.setTo(propColourBlindMode);

        propWorldGen = config.get(worldgen, "enable", true);
        propWorldGen.setComment("Should BuildCraft generate anything in the world?");
        game.setTo(propWorldGen);

        propWorldGenWaterSpring = config.get(worldgen, "generateWaterSprings", true);
        propWorldGenWaterSpring.setComment("Should BuildCraft generate water springs?");
        game.setTo(propWorldGenWaterSpring);

        propUseLocalServerOnClient = config.get(general, "useServerDataOnClient", true);
        propUseLocalServerOnClient.setComment("Allows BuildCraft to use the integrated server's data on the client on singleplayer worlds. Disable if you're getting the odd crash caused by it.");
        none.setTo(propUseLocalServerOnClient);

        propMinePlayerProtected = config.get(general, "miningBreaksPlayerProtectedBlocks", false);
        propMinePlayerProtected.setComment("Should BuildCraft miners be allowed to break blocks using player-specific protection?");
        none.setTo(propMinePlayerProtected);

        propHidePower = config.get(display, "hidePowerValues", false);
        propHidePower.setComment("Should all power values (MJ, MJ/t) be hidden?");
        none.setTo(propHidePower);

        propHideFluid = config.get(display, "hideFluidValues", false);
        propHideFluid.setComment("Should all fluid values (Buckets, mB, mB/t) be hidden?");
        none.setTo(propHideFluid);

        propUseBucketsStatic = config.get(display, "useBucketsStatic", false);
        propUseBucketsStatic.setComment("Should static fluid values be displayed in terms of buckets rather than thousandths of a bucket? (B vs mB)");
        none.setTo(propUseBucketsStatic);

        propUseBucketsFlow = config.get(display, "useBucketsFlow", false);
        propUseBucketsFlow.setComment("Should flowing fluid values be displayed in terms of buckets per second rather than thousandths of a bucket per tick? (B/s vs mB/t)");
        none.setTo(propUseBucketsFlow);

        propItemLifespan = config.get(general, "itemLifespan", 60);
        propItemLifespan.setMinValue(5).setMaxValue(600);
        propItemLifespan.setComment("How long, in seconds, should items stay on the ground? (Vanilla = 300, default = 60)");
        none.setTo(propItemLifespan);

        propMarkerMaxDistance = config.get(general, "markerMaxDistance", 64);
        propMarkerMaxDistance.setMinValue(16).setMaxValue(256);
        propMarkerMaxDistance.setComment("How far, in minecraft blocks, should markers (volume and path) reach?");
        none.setTo(propMarkerMaxDistance);

        reloadConfig(game);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        useLocalServerOnClient = propUseLocalServerOnClient.getBoolean();
        minePlayerProteted = propMinePlayerProtected.getBoolean();
        hidePower = propHidePower.getBoolean();
        hideFluid = propHideFluid.getBoolean();
        useBucketsStatic = propUseBucketsStatic.getBoolean();
        useBucketsFlow = propUseBucketsFlow.getBoolean();
        itemLifespan = propItemLifespan.getInt();
        markerMaxDistance = propMarkerMaxDistance.getInt();

        if (EnumRestartRequirement.GAME.hasBeenRestarted(restarted)) {
            colourBlindMode = propColourBlindMode.getBoolean();
            worldGen = propWorldGen.getBoolean();
            worldGenWaterSpring = propWorldGenWaterSpring.getBoolean();
        }
    }
}
