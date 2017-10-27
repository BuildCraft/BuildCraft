/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.client;

import java.util.ArrayList;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import buildcraft.core.BCCoreConfig;

public class ConfigGuiFactoryBC implements IModGuiFactory {
    public static class GuiConfigManager extends GuiConfig {
        public GuiConfigManager(GuiScreen parentScreen) {
            super(parentScreen, new ArrayList<>(), "buildcraftcore", "config", false, false, I18n.format("config.buildcraft"));

            for (String s : BCCoreConfig.config.getCategoryNames()) {
                if (!s.contains(".")) {
                    configElements.add(new BCConfigElement(BCCoreConfig.config.getCategory(s)));
                }
            }

            for (String s : BCCoreConfig.objConfig.getCategoryNames()) {
                if (!s.contains(".")) {
                    configElements.add(new BCConfigElement(BCCoreConfig.objConfig.getCategory(s)));
                }
            }
        }
    }

    /** Needed for forge IModGuiFactory */
    public ConfigGuiFactoryBC() {}

    @Override
    public void initialize(Minecraft minecraftInstance) {
        // We don't need to do anything
    }


    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }


    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new GuiConfigManager(parentScreen);
    }
}
