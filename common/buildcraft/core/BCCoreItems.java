/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.core;

import buildcraft.core.item.*;
import buildcraft.lib.BCLibItems;
import buildcraft.lib.CreativeTabManager;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;

public class BCCoreItems {
    public static ItemBC_Neptune gearWood;
    public static ItemBC_Neptune gearStone;
    public static ItemBC_Neptune gearIron;
    public static ItemBC_Neptune gearGold;
    public static ItemBC_Neptune gearDiamond;
    public static ItemPaintbrush_BC8 paintbrush;
    public static ItemWrench_Neptune wrench;
    public static ItemList_BC8 list;
    public static ItemMapLocation mapLocation;
    public static ItemMarkerConnector markerConnector;

    public static void preInit() {
        gearWood = ItemManager.register(new ItemBC_Neptune("item.gear.wood"));
        gearStone = ItemManager.register(new ItemBC_Neptune("item.gear.stone"));
        gearIron = ItemManager.register(new ItemBC_Neptune("item.gear.iron"));
        gearGold = ItemManager.register(new ItemBC_Neptune("item.gear.gold"));
        gearDiamond = ItemManager.register(new ItemBC_Neptune("item.gear.diamond"));
        paintbrush = ItemManager.register(new ItemPaintbrush_BC8("item.paintbrush"));
        wrench = ItemManager.register(new ItemWrench_Neptune("item.wrench"));
        list = ItemManager.register(new ItemList_BC8("item.list"));
        mapLocation = ItemManager.register(new ItemMapLocation("item.map_location"));
        markerConnector = ItemManager.register(new ItemMarkerConnector("item.marker_connector"));

        BCLibItems.guide.setCreativeTab(CreativeTabManager.getTab("buildcraft.main"));
    }
}
