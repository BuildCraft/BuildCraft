/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.core.item.*;
import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;
import buildcraft.lib.registry.CreativeTabManager;

public class BCCoreItems {

    public static ItemWrench_Neptune wrench;
    public static ItemBC_Neptune gearWood;
    public static ItemBC_Neptune gearStone;
    public static ItemBC_Neptune gearIron;
    public static ItemBC_Neptune gearGold;
    public static ItemBC_Neptune gearDiamond;
    public static ItemBC_Neptune diamondShard;
    public static ItemPaintbrush_BC8 paintbrush;
    public static ItemList_BC8 list;
    public static ItemMapLocation mapLocation;
    public static ItemMarkerConnector markerConnector;
    public static ItemVolumeBox volumeBox;
    public static ItemGoggles goggles;

    public static void preInit() {
        wrench = ItemManager.register(new ItemWrench_Neptune("item.wrench"));
        gearWood = ItemManager.register(new ItemBC_Neptune("item.gear.wood"));
        gearStone = ItemManager.register(new ItemBC_Neptune("item.gear.stone"));
        gearIron = ItemManager.register(new ItemBC_Neptune("item.gear.iron"));
        gearGold = ItemManager.register(new ItemBC_Neptune("item.gear.gold"));
        gearDiamond = ItemManager.register(new ItemBC_Neptune("item.gear.diamond"));
        if (BCLib.DEV) {
            diamondShard = ItemManager.register(new ItemBC_Neptune("item.diamond.shard"));
        }
        paintbrush = ItemManager.register(new ItemPaintbrush_BC8("item.paintbrush"));
        list = ItemManager.register(new ItemList_BC8("item.list"));
        if (BCLib.DEV) {
            mapLocation = ItemManager.register(new ItemMapLocation("item.map_location"));
            markerConnector = ItemManager.register(new ItemMarkerConnector("item.marker_connector"));
        }
        volumeBox = ItemManager.register(new ItemVolumeBox("item.volume_box"));
        if (BCLib.DEV) {
            goggles = ItemManager.register(new ItemGoggles("item.goggles"));
        }
        BCLibItems.guide.setCreativeTab(CreativeTabManager.getTab("buildcraft.main"));
    }
}
