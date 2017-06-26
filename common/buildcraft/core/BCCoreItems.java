/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import java.util.Arrays;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.IItemBuildCraft;
import buildcraft.lib.item.ItemBC_Neptune;

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

    public static final ItemWrench_Neptune wrench = null;
    @GameRegistry.ObjectHolder("gear_wood")
    public static final ItemBC_Neptune gearWood = null;
    @GameRegistry.ObjectHolder("gear_stone")
    public static final ItemBC_Neptune gearStone = null;
    @GameRegistry.ObjectHolder("gear_iron")
    public static final ItemBC_Neptune gearIron = null;
    @GameRegistry.ObjectHolder("gear_gold")
    public static final ItemBC_Neptune gearGold = null;
    @GameRegistry.ObjectHolder("gear_diamond")
    public static final ItemBC_Neptune gearDiamond = null;
    @GameRegistry.ObjectHolder("diamond_shard")
    public static final ItemBC_Neptune diamondShard = null;
    public static final ItemPaintbrush_BC8 paintbrush = null;
    public static final ItemList_BC8 list = null;
    @GameRegistry.ObjectHolder("map_location")
    public static final ItemMapLocation mapLocation = null;
    @GameRegistry.ObjectHolder("marker_connector")
    public static final ItemMarkerConnector markerConnector = null;
    @GameRegistry.ObjectHolder("volume_box")
    public static final ItemVolumeBox volumeBox = null;
    public static final ItemGoggles goggles = null;

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
        IItemBuildCraft[] items = {
            wrench,
            gearWood,
            gearStone,
            gearIron,
            gearGold,
            gearDiamond,
            diamondShard,
            paintbrush,
            list,
            mapLocation,
            markerConnector,
            volumeBox,
            goggles
        };
        Arrays.stream(items).forEach(IItemBuildCraft::registerVariants);
    }
}
