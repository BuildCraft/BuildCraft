/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.api.items.FluidItemDrops;

import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.registry.RegistrationHelper;

import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.core.item.ItemGoggles;
import buildcraft.core.item.ItemList_BC8;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemMarkerConnector;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.core.item.ItemVolumeBox;
import buildcraft.core.item.ItemWrench_Neptune;

public class BCCoreItems {

    public static ItemWrench_Neptune wrench;
    public static ItemBC_Neptune gearWood;
    public static ItemBC_Neptune gearStone;
    public static ItemBC_Neptune gearIron;
    public static ItemBC_Neptune gearGold;
    public static ItemBC_Neptune gearDiamond;
    public static ItemPaintbrush_BC8 paintbrush;
    public static ItemList_BC8 list;
    public static ItemMapLocation mapLocation;
    public static ItemMarkerConnector markerConnector;
    public static ItemVolumeBox volumeBox;
    public static ItemFragileFluidContainer fragileFluidShard;
    public static ItemGoggles goggles;

    public static void preInit() {
        wrench = RegistrationHelper.addItem(new ItemWrench_Neptune("item.wrench"));
        gearWood = RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.wood"));
        gearStone = RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.stone"));
        gearIron = RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.iron"));
        gearGold = RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.gold"));
        gearDiamond = RegistrationHelper.addItem(new ItemBC_Neptune("item.gear.diamond"));
        paintbrush = RegistrationHelper.addItem(new ItemPaintbrush_BC8("item.paintbrush"));
        list = RegistrationHelper.addItem(new ItemList_BC8("item.list"));
        mapLocation = RegistrationHelper.addItem(new ItemMapLocation("item.map_location"));
        markerConnector = RegistrationHelper.addItem(new ItemMarkerConnector("item.marker_connector"));
        volumeBox = RegistrationHelper.addItem(new ItemVolumeBox("item.volume_box"));
        fragileFluidShard = RegistrationHelper.addItem(new ItemFragileFluidContainer("item.fragile_fluid_shard"));
        if (BCLib.DEV) {
            goggles = RegistrationHelper.addItem(new ItemGoggles("item.goggles"));
        }
        FluidItemDrops.item = fragileFluidShard;
    }
}
