/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.api.BCModules;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.config.EnumRestartRequirement;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BCSiliconConfig {

    public static Boolean renderLaserBeams = true;

    private static Property propRenderLaserBeams;

    public static void preInit() {

        Configuration config = BCCoreConfig.config;
        //TODO change the category and update the comment once visible with goggles is implemented
        propRenderLaserBeams = config.get("experimental", "renderLaserBeams", true,
                "When set to false Laser beams will not be visible. Default: true");
        EnumRestartRequirement.WORLD.setTo(propRenderLaserBeams);

        MinecraftForge.EVENT_BUS.register(BCSiliconConfig.class);
    }

    public static void reloadConfig(EnumRestartRequirement restarted) {
        if (EnumRestartRequirement.WORLD.hasBeenRestarted(restarted)) {
            renderLaserBeams = propRenderLaserBeams.getBoolean();
        }
    }

    @SubscribeEvent
    public static void onConfigChange(OnConfigChangedEvent cce) {
        if (BCModules.isBcMod(cce.getModID())) {
            EnumRestartRequirement req = EnumRestartRequirement.NONE;
            if (Loader.instance().isInState(LoaderState.AVAILABLE)) {
                // The loaders state will be LoaderState.SERVER_STARTED when we are in a world
                req = EnumRestartRequirement.WORLD;
            }
            reloadConfig(req);
        }
    }
}
