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
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.transport.pipe.IItemPipe;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.IItemBuildCraft;

public class RegistryHelper {
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
    // Registration
    //
    // #######################

    public static boolean registerItem(Item item) {
        return registerItem(item, false);
    }

    public static boolean registerItem(Item item, boolean forced) {
        if (forced || isEnabled(getCategory(item), item.getRegistryName().getResourcePath(), item.getUnlocalizedName() + ".name")) {
            GameRegistry.register(item);
            if (item instanceof IItemBuildCraft) {
                IItemBuildCraft itemBc = (IItemBuildCraft) item;
                BCLibProxy.getProxy().postRegisterItem(itemBc);
            }
            return true;
        }
        return false;
    }

    public static boolean registerBlock(Block block) {
        return registerBlock(block, false);
    }

    public static boolean registerBlock(Block block, boolean forced) {
        if (forced || isEnabled("blocks", block.getRegistryName().getResourcePath(), block.getUnlocalizedName() + ".name")) {
            GameRegistry.register(block);
            if (block instanceof BlockBCBase_Neptune) {
                BlockBCBase_Neptune blockBc = (BlockBCBase_Neptune) block;
                BCLibProxy.getProxy().postRegisterBlock(blockBc);
            }
            return true;
        }
        return false;
    }

    public static boolean isEnabled(String category, String resourcePath, String langKey) {
        return isEnabled(getActiveMod(), category, resourcePath, langKey);
    }

    // #######################
    //
    // Internals
    //
    // #######################

    private static String getCategory(Item item) {
        if (item instanceof IItemPipe) {
            return "pipes";
        } else {
            return "items";
        }
    }

    private static boolean isEnabled(ModContainer activeMod, String category, String resourcePath, String langKey) {
        Configuration config = modObjectConfigs.get(activeMod);
        if (config == null) throw new RuntimeException("No config exists for the mod " + activeMod.getModId());
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
