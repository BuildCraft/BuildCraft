/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.registry;

import buildcraft.api.core.BCDebugging;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.config.ConfigCategory;
import buildcraft.lib.config.Configuration;
import buildcraft.lib.config.EnumRestartRequirement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RegistryConfig {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.registry");
    private static final Map<ModContainer, Configuration> modObjectConfigs = new IdentityHashMap<>();
    private static final Map<String, Set<String>> disabled = new HashMap<>();

    // #######################
    //
    // Setup
    //
    // #######################

    public static Configuration setRegistryConfig(String modid, String file) {
        Configuration cfg = new Configuration(file);
        return setRegistryConfig(modid, cfg);
    }

    public static Configuration setRegistryConfig(String modid, Configuration config) {
        modObjectConfigs.put(getMod(modid), config);
        return config;
    }

    // Calen: this method always causes java.lang.IllegalStateException: Didn't find a config for buildcraftcore
    // at buildcraftcore.lib.registry.RegistryConfig.useOtherModConfigFor(RegistryConfig.java:46) ~[%2387!/:?] {re:classloading}
//    public static Configuration useOtherModConfigFor(String from, String to)
    public static void useOtherModConfigFor(String from, String to) {
//        Configuration config = modObjectConfigs.get(getMod(to));
//        if (config == null) {
//            throw new IllegalStateException("Didn't find a config for " + to);
//        }
//        modObjectConfigs.put(getMod(from), config);
//        return config;
        moduleConfigMapping.put(getMod(from), getMod(to));
    }

    // #######################
    //
    // Checking
    //
    // #######################

    // public static boolean isEnabled(Item item)
    public static boolean isEnabledItem(String idBC) {
//        return isEnabled(getCategory(item), item.getRegistryName().getPath(),
//                item.getRegistryName() + ".name");
        String regPath = TagManager.getTag(idBC, TagManager.EnumTagType.REGISTRY_NAME).split(":")[1];
        return isEnabled("items", regPath, "item." + TagManager.getTag(idBC).getSingleTag(TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
    }

    // Calen: in 1.12.2 pipe items are forced
    // this #isEnabled is never used on pipes
    public static boolean isEnabledPipeItemItem(String idBC) {
        String regPath = TagManager.getTag(idBC, TagManager.EnumTagType.REGISTRY_NAME).split(":")[1];
        return isEnabled("pipes", regPath, "item." + TagManager.getTag(idBC).getSingleTag(TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
    }

    // public static boolean isEnabled(Block block)
    public static boolean isEnabledBlock(String idBC) {
//        return isEnabled(getCategory(block), block.getRegistryName().getPath(),
//                block.getRegistryName() + ".name");
        String regPath = TagManager.getTag(idBC, TagManager.EnumTagType.REGISTRY_NAME).split(":")[1];
        return isEnabled("blocks", regPath, "tile." + TagManager.getTag(idBC).getSingleTag(TagManager.EnumTagType.UNLOCALIZED_NAME) + ".name");
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

    /**
     * @return True if the given location has been passed to {@link #isEnabledBlock(String)}, {@link #isEnabledItem(String)}, or
     * {@link #isEnabled(String, String, String)}, and it returned false (because it has been disabled in the
     * appropriate mod's config)
     */
    public static boolean hasObjectBeenDisabled(String category, ResourceLocation loc) {
        Set<String> locations = disabled.get(category);
        return locations != null && locations.contains(loc.getPath());
    }

    // #######################
    //
    // Internals
    //
    // #######################

//    private static String getCategory(Object obj) {
//        if (obj instanceof IItemPipe) {
//            return "pipes";
//        } else if (obj instanceof Block) {
//            return "blocks";
//        } else {
//            return "items";
//        }
//    }

    // Calen: Thread Safety
    private static ConcurrentHashMap<ModContainer, ModContainer> moduleConfigMapping = new ConcurrentHashMap<>();

    // Calen
    private static Map<ModContainer, Configuration> getModObjectConfigs() {
        // just ensure Core Config loaded
        BCCoreConfig.cinit();
        // ret
        return modObjectConfigs;
    }

    private synchronized static boolean isEnabled(ModContainer activeMod, String category, String resourcePath, String langKey) {
//        Configuration config = modObjectConfigs.get(activeMod);
        Configuration config = getModObjectConfigs().get(activeMod);
        if (config == null) {
//            config = modObjectConfigs.get(moduleConfigMapping.get(activeMod));
            config = getModObjectConfigs().get(moduleConfigMapping.get(activeMod));
            if (config == null) {
                throw new RuntimeException("No config exists for the mod " + activeMod.getModId());
            }
        }

        ConfigCategory<Boolean> prop = config
                .define(category,
                        "",
                        EnumRestartRequirement.WORLD,
                        resourcePath, true);
        prop.setLanguageKey(langKey);
        boolean isEnabled = prop.get();
        if (!isEnabled) {
            setDisabled(category, resourcePath);
        }
        return isEnabled;
    }

    static void setDisabled(String category, String resourcePath) {
        disabled.computeIfAbsent(category, k -> new HashSet<>()).add(resourcePath);
    }

    private static ModContainer getMod(String modid) {
        ModContainer container = ModList.get().getModContainerById(modid).get();
//        if (container == null)
        if (container == null || !(container instanceof FMLModContainer)) {
            throw new RuntimeException("No mod with an id of \"" + modid + "\" is loaded!");
        } else {
            return container;
        }
    }

    private static ModContainer getActiveMod() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        if (container == null) {
            throw new RuntimeException("Was not called within the scope of an active mod!");
        } else {
            return container;
        }
    }
}
