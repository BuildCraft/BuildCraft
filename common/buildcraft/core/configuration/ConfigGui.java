package buildcraft.core.configuration;

import buildcraft.BuildCraftCore;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parentScreen) {
        super(parentScreen, new ConfigElement(BuildCraftCore.mainConfiguration.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), "BuildCraft|Core", false, false, GuiConfig.getAbridgedConfigPath(BuildCraftCore.mainConfiguration.toString()));
    }
}
