/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.transport.pipe.IItemPipe;

public class RegistryConfig {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.registry");
    private static final Map<ModContainer, Configuration> modObjectConfigs = new IdentityHashMap<>();
    private static final Map<String, Set<String>> disabled = new HashMap<>();

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

    public static boolean hasItemBeenDisabled(ResourceLocation loc) {
        return hasObjectBeenDisabled("items", loc) || hasObjectBeenDisabled("pipes", loc);
    }

    public static boolean hasBlockBeenDisabled(ResourceLocation loc) {
        return hasObjectBeenDisabled("blocks", loc);
    }

    /** @return True if the given location has been passed to {@link #isEnabled(Block)}, {@link #isEnabled(Item)}, or
     *         {@link #isEnabled(String, String, String)}, and it returned false (because it has been disabled in the
     *         appropriate mod's config) */
    public static boolean hasObjectBeenDisabled(String category, ResourceLocation loc) {
        Set<String> locations = disabled.get(category);
        return locations != null && locations.contains(loc.getResourcePath());
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
        boolean isEnabled = prop.getBoolean(true);
        if (!isEnabled) {
            setDisabled(category, resourcePath);
        }
        return isEnabled;
    }

    static void setDisabled(String category, String resourcePath) {
        disabled.computeIfAbsent(category, k -> new HashSet<>()).add(resourcePath);
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
