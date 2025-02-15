/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.api.BCModules;
import buildcraft.lib.config.BCConfig;
import buildcraft.lib.config.ConfigCategory;
import buildcraft.lib.config.Configuration;
import buildcraft.lib.config.EnumRestartRequirement;

public class BCSiliconConfig {
    private static Configuration config;

    public static boolean renderLaserBeams = true;
    public static boolean differStatesOfNoteBlockForFacade = false;

    private static ConfigCategory<Boolean> propRenderLaserBeams;
    private static ConfigCategory<Boolean> propDifferStatesOfNoteBlockForFacade;

    public static void preInit() {
//        Configuration config = BCCoreConfig.config;
        BCModules module = BCModules.SILICON;
        config = new Configuration(module);
        createProps();

//        reloadConfig(EnumRestartRequirement.NONE);
        reloadConfig();
//        MinecraftForge.EVENT_BUS.register(BCSiliconConfig.class);
        BCConfig.registerReloadListener(module, BCSiliconConfig::reloadConfig);
    }

    public static void createProps() {
        String display = "display";

        propRenderLaserBeams = config
                .define(display,
                        "When false laser beams will not be visible while transmitting power without wearing Goggles",
                        EnumRestartRequirement.NONE,
                        "renderLaserBeams", true);
        propDifferStatesOfNoteBlockForFacade = config
                .define(display,
                        "If different textures in resource packs are used for different instruments and notes, or whether powered, please set this [true]",
                        EnumRestartRequirement.WORLD,
                        "differStatesOfNoteBlockForFacade", false);
    }

    // public static void reloadConfig(EnumRestartRequirement restarted)
    public static void reloadConfig() {
        renderLaserBeams = propRenderLaserBeams.get();
        differStatesOfNoteBlockForFacade = propDifferStatesOfNoteBlockForFacade.get();

        saveConfigs();
    }

    public static void saveConfigs() {
        if (config.hasChanged()) {
            config.save();
        }
    }

//    @SubscribeEvent
//    public static void onConfigChange(OnConfigChangedEvent cce) {
//        if (BCModules.isBcMod(cce.getModID())) {
//            reloadConfig(EnumRestartRequirement.NONE);
//        }
//    }
}
