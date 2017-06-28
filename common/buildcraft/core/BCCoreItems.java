/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.registry.RegistryHelper;

import buildcraft.core.item.ItemGoggles;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemMarkerConnector;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.core.item.ItemVolumeBox;
import buildcraft.core.item.ItemWrench_Neptune;

@Mod.EventBusSubscriber(modid = BCCore.MODID)
@GameRegistry.ObjectHolder(BCCore.MODID)
public class BCCoreItems {

    public static final ItemWrench_Neptune WRENCH = null;
    public static final ItemBC_Neptune GEAR_WOOD = null;
    public static final ItemBC_Neptune GEAR_STONE = null;
    public static final ItemBC_Neptune GEAR_IRON = null;
    public static final ItemBC_Neptune GEAR_GOLD = null;
    public static final ItemBC_Neptune GEAR_DIAMOND = null;
    public static final ItemBC_Neptune DIAMOND_SHARD = null;
    public static final ItemPaintbrush_BC8 PAINTBRUSH = null;
    public static final ItemList_BC8 LIST = null;
    public static final ItemMapLocation MAP_LOCATION = null;
    public static final ItemMarkerConnector MARKER_CONNECTOR = null;
    public static final ItemVolumeBox VOLUME_BOX = null;
    public static final ItemGoggles GOGGLES = null;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            new ItemWrench_Neptune("item.wrench"),
            new ItemBC_Neptune("item.gear.wood"),
            new ItemBC_Neptune("item.gear.stone"),
            new ItemBC_Neptune("item.gear.iron"),
            new ItemBC_Neptune("item.gear.gold"),
            new ItemBC_Neptune("item.gear.diamond"),
            new ItemPaintbrush_BC8("item.paintbrush"),
            new ItemList_BC8("item.list"),
            new ItemMarkerConnector("item.marker_connector"),
            new ItemVolumeBox("item.volume_box")
        );
        if (BCLib.DEV) {
            event.getRegistry().registerAll(
                new ItemBC_Neptune("item.diamond.shard"),
                new ItemMapLocation("item.map_location"),
                new ItemGoggles("item.goggles")
            );

        }
    }

    @SubscribeEvent
    public static void registerVariants(ModelRegistryEvent event) {
        RegistryHelper.registerVariants(
            WRENCH,
            GEAR_WOOD,
            GEAR_STONE,
            GEAR_IRON,
            GEAR_GOLD,
            GEAR_DIAMOND,
            DIAMOND_SHARD,
            PAINTBRUSH,
            LIST,
            MAP_LOCATION,
            MARKER_CONNECTOR,
            VOLUME_BOX,
            GOGGLES
        );
    }
}
