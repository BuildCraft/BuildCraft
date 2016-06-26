/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
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
