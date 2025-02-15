/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.config.ConfigCategory;
import net.minecraft.client.gui.screens.Screen;

// TODO Calen ConfigGui?
//public class ConfigGuiFactoryBC implements ModGuiFactory
public class ConfigGuiFactoryBC {
    // public static class GuiConfigManager extends GuiConfig
    public static class GuiConfigManager {
        // public GuiConfigManager(GuiScreen parentScreen)
        public GuiConfigManager(Screen parentScreen) {
//            super(parentScreen, new ArrayList<>(), "buildcraftcore", "config", false, false, new TranslatableComponent("config.buildcraftcore").getContents());

//            for (String s : BCCoreConfig.config.getCategoryNames())
            for (ConfigCategory<?> s : BCCoreConfig.config.getAll()) {
                if (s.getFullPath().split("\\.").length == 1) {
//                    configElements.add(new BCConfigElement(BCCoreConfig.config.getCategory(s)));
                }
            }

//            for (String s : BCCoreConfig.objConfig.getCategoryNames())
            for (ConfigCategory<?> s : BCCoreConfig.objConfig.getAll()) {
                if (s.getFullPath().split("\\.").length == 1) {
//                    configElements.add(new BCConfigElement(BCCoreConfig.objConfig.getCategory(s)));
                }
            }
        }
    }

    /**
     * Needed for forge IModGuiFactory
     */
    public ConfigGuiFactoryBC() {
    }

//    @Override
//    public void initialize(Minecraft minecraftInstance) {
//        // We don't need to do anything
//    }


//    @Override
//    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
//        return null;
//    }


//    @Override
//    public boolean hasConfigGui() {
//        return true;
//    }

//    @Override
//    public GuiScreen createConfigGui(Gui parentScreen) {
//        return new GuiConfigManager(parentScreen);
//    }
}
