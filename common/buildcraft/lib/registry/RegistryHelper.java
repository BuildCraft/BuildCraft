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
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import buildcraft.api.transport.pipe.IItemPipe;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBlockBC_Neptune;

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

    public static void registerItems(RegistryEvent.Register<Item> event, Object...items) {
        for (Object o: items) {
            Item item = null;
            if (o instanceof Item) {
                item = (Item) o;
            } else if (o instanceof BlockBCBase_Neptune) {
                item = new ItemBlockBC_Neptune((BlockBCBase_Neptune) o);
            }
            if (item != null)
                event.getRegistry().register(item);
        }
    }

    public static void registerVariants(Object...items) {
        for (Object o : items) {
            if (o instanceof Block) {
                o = Item.getItemFromBlock((Block) o);
            }
            if (o instanceof IItemBuildCraft) {
                ((IItemBuildCraft) o).registerVariants();
            }

        }
    }

    public static boolean isEnabled(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            return isEnabled(((ItemBlock) item).getBlock());
        }
        return isEnabled(item);
    }

    public static boolean isEnabled(Item item) {
        return isEnabled(item.getRegistryName().getResourceDomain(), getCategory(item), item.getRegistryName().getResourcePath(), item.getUnlocalizedName() + ".name");
    }
    public static boolean isEnabled(Block block) {
        return isEnabled(block.getRegistryName().getResourceDomain(), getCategory(block), block.getRegistryName().getResourcePath(), block.getUnlocalizedName() + ".name");
    }

    private static boolean isEnabled(String modid, String category, String resourcePath, String langKey) {
        return isEnabled(getMod(modid), category, resourcePath, langKey);
    }

    // #######################
    //
    // Internals
    //
    // #######################

    private static String getCategory(Object item) {
        if (item instanceof IItemPipe) {
            return "pipes";
        } else if (item instanceof Block) {
            return "blocks";
        } else {
            return "items";
        }
    }

    public static boolean isEnabled(ModContainer activeMod, String category, String resourcePath, String langKey) {
        Configuration config = modObjectConfigs.get(activeMod);
        if (config == null) return false; //throw new RuntimeException("No config exists for the mod " + activeMod.getModId());
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
}
