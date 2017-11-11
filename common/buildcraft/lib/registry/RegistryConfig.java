/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.transport.pipe.IItemPipe;

public class RegistryConfig {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.registry");
    private static final Map<ModContainer, Configuration> modObjectConfigs = new IdentityHashMap<>();

    // #######################
    //
    // Setup
    //
    // #######################

    public static Configuration setRegistryConfig(String modid, File file) {
        Configuration cfg = new Configuration(file);
        return setRegistryConfig(modid, cfg);
    }

    public static Configuration setRegistryConfig(String modid, Configuration config) {
        modObjectConfigs.put(getMod(modid), config);
        return config;
    }

    public static Configuration useOtherModConfigFor(String from, String to) {
        Configuration config = modObjectConfigs.get(getMod(to));
        if (config == null) {
            throw new IllegalStateException("Didn't find a config for " + to);
        }
        modObjectConfigs.put(getMod(from), config);
        return config;
    }

    // #######################
    //
    // Checking
    //
    // #######################

    public static boolean isEnabled(Item item) {
        return isEnabled(getCategory(item), item.getRegistryName().getResourcePath(),
            item.getUnlocalizedName() + ".name");
    }

    public static boolean isEnabled(Block block) {
        return isEnabled(getCategory(block), block.getRegistryName().getResourcePath(),
            block.getUnlocalizedName() + ".name");
    }

    public static boolean isEnabled(String category, String resourcePath, String langKey) {
        return isEnabled(getActiveMod(), category, resourcePath, langKey);
    }

    // #######################
    //
    // Internals
    //
    // #######################

    private static String getCategory(Object obj) {
        if (obj instanceof IItemPipe) {
            return "pipes";
        } else if (obj instanceof Block) {
            return "blocks";
        } else {
            return "items";
        }
    }

    private static boolean isEnabled(ModContainer activeMod, String category, String resourcePath, String langKey) {
        Configuration config = modObjectConfigs.get(activeMod);
        if (config == null) {
            throw new RuntimeException("No config exists for the mod " + activeMod.getModId());
        }
        Property prop = config.get(category, resourcePath, true);
        prop.setLanguageKey(langKey);
        prop.setRequiresMcRestart(true);
        prop.setRequiresWorldRestart(true);
        return prop.getBoolean(true);
    }

    private static ModContainer getMod(String modid) {
        ModContainer container = Loader.instance().getIndexedModList().get(modid);
        if (container == null) {
            throw new RuntimeException("No mod with an id of \"" + modid + "\" is loaded!");
        } else {
            return container;
        }
    }

    private static ModContainer getActiveMod() {
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null) {
            throw new RuntimeException("Was not called within the scope of an active mod!");
        } else {
            return container;
        }
    }
}
