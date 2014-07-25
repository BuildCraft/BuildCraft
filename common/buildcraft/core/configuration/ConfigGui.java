/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.configuration;

import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.client.config.GuiConfig;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import buildcraft.BuildCraftCore;

public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(BuildCraftCore.mainConfiguration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), "BuildCraft|Core", false, false, GuiConfig.getAbridgedConfigPath(BuildCraftCore.mainConfiguration.toString()));
    }
}
