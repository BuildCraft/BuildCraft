/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import java.io.File;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.ItemBC_Neptune;

public class RegistryHelper {
    private static final Map<ModContainer, Configuration> modObjectConfigs = new IdentityHashMap<>();

    public static void setRegistryConfig(String modid, File file) {
        setRegistryConfig(modid, new Configuration(file));
    }

    public static void setRegistryConfig(String modid, Configuration config) {
        modObjectConfigs.put(getMod(modid), config);
    }

    public static void useOtherModConfigFor(String from, String to) {
        modObjectConfigs.put(getMod(from), modObjectConfigs.get(getMod(to)));
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
        if (forced || isEnabled(getActiveMod(), "items", item.getRegistryName().getResourcePath())) {
            GameRegistry.register(item);
            if (item instanceof ItemBC_Neptune) {
                ItemBC_Neptune itemBc = (ItemBC_Neptune) item;
                LibProxy.getProxy().postRegisterItem(itemBc);
            }
            return true;
        }
        return false;
    }

    public static boolean registerBlock(Block block) {
        return registerBlock(block, false);
    }

    public static boolean registerBlock(Block block, boolean forced) {
        if (forced || isEnabled(getActiveMod(), "blocks", block.getRegistryName().getResourcePath())) {
            GameRegistry.register(block);
            if (block instanceof BlockBCBase_Neptune) {
                BlockBCBase_Neptune blockBc = (BlockBCBase_Neptune) block;
                LibProxy.getProxy().postRegisterBlock(blockBc);
            }
            return true;
        }
        return false;
    }

    // #######################
    //
    // Internals
    //
    // #######################

    private static boolean isEnabled(ModContainer activeMod, String category, String resourcePath) {
        Configuration config = modObjectConfigs.get(activeMod);
        if (config == null) throw new RuntimeException("No config exists for the mod " + activeMod.getModId());
        return config.get(category, resourcePath, true).getBoolean(true);
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
