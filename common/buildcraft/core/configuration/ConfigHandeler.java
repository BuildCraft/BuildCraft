/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.configuration;

import net.minecraftforge.common.config.Configuration;

import buildcraft.BuildCraftCore;
import buildcraft.core.Version;

public final class ConfigHandeler {

    private ConfigHandeler() {

    }

    public static void readConfiguration() {
        try {
            if (BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "update.check", true, "set to true for version check on startup").getBoolean()) {
                Version.check();
            }
            BuildCraftCore.dropBrokenBlocks = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "dropBrokenBlocks", true, "set to false to prevent fillers from dropping blocks.").getBoolean();
            BuildCraftCore.itemLifespan = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "itemLifespan", BuildCraftCore.itemLifespan, "the lifespan in ticks of items dropped on the ground by pipes and machines, vanilla = 6000, default = 1200", 100, 100000).getInt();
            BuildCraftCore.updateFactor = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "network.updateFactor", 10, "increasing this number will decrease network update frequency, useful for overloaded servers").getInt();
            BuildCraftCore.longUpdateFactor = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "network.stateRefreshPeriod", 40, "delay between full client sync packets, increasing it saves bandwidth, decreasing makes for better client syncronization.").getInt();
            BuildCraftCore.modifyWorld = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "modifyWorld", true, "set to false if BuildCraft should not generate custom blocks (e.g. oil)").setRequiresMcRestart(true).getBoolean();
            BuildCraftCore.consumeWaterSources = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "consumeWater", BuildCraftCore.consumeWaterSources, "Set to true to use the water sources").getBoolean();
        } finally {
            BuildCraftCore.mainConfiguration.save();
        }
    }
}
