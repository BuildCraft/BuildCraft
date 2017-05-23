/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.registry.MigrationManager;
import buildcraft.lib.registry.RegistryHelper;
import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;
import buildcraft.lib.registry.TagManager.EnumTagTypeMulti;

import gnu.trove.map.hash.TIntObjectHashMap;

public class ItemManager {
    static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.item");
    private static List<IItemBuildCraft> registeredItems = new ArrayList<>();

    public static <I extends Item & IItemBuildCraft> I register(I item) {
        return register(item, false);
    }

    public static <I extends Item & IItemBuildCraft> I register(I item, boolean force) {
        if (RegistryHelper.registerItem(item, force)) {
            registeredItems.add(item);
            MigrationManager.INSTANCE.addItemMigration(item, TagManager.getMultiTag(item.id(), EnumTagTypeMulti.OLD_REGISTRY_NAME));
            return item;
        }
        return null;
    }

    public static void fmlInit() {
        for (IItemBuildCraft item : registeredItems) {
            if (TagManager.hasTag(item.id(), EnumTagType.OREDICT_NAME)) {
                OreDictionary.registerOre(TagManager.getTag(item.id(), EnumTagType.OREDICT_NAME), (Item) item);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void fmlInitClient() {
        for (IItemBuildCraft item : registeredItems) {
            TIntObjectHashMap<ModelResourceLocation> variants = new TIntObjectHashMap<>();
            item.addModelVariants(variants);
            for (int meta : variants.keys()) {
                ModelResourceLocation mrl = variants.get(meta);
                if (DEBUG) {
                    BCLog.logger.info("[lib.item][" + ((Item) item).getRegistryName() + "] Registering a variant " + meta + " -> " + mrl);
                }
                Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register((Item) item, meta, mrl);
            }
        }
    }
}
