package buildcraft.core;

import buildcraft.core.item.*;
import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.lib.item.ItemManager;

public class BCCoreItems {
    public static ItemBuildCraft_BC8 gearWood;
    public static ItemBuildCraft_BC8 gearStone;
    public static ItemBuildCraft_BC8 gearIron;
    public static ItemBuildCraft_BC8 gearGold;
    public static ItemBuildCraft_BC8 gearDiamond;
    public static ItemGuide guide;
    public static ItemPaintbrush_BC8 paintbrush;
    public static ItemWrench_BC8 wrench;
    public static ItemList_BC8 list;
    public static ItemMapLocation mapLocation;
    public static ItemMarkerConnector markerConnector;

    public static void preInit() {
        gearWood = ItemManager.register(new ItemBuildCraft_BC8("item.gear.wood"));
        gearStone = ItemManager.register(new ItemBuildCraft_BC8("item.gear.stone"));
        gearIron = ItemManager.register(new ItemBuildCraft_BC8("item.gear.iron"));
        gearGold = ItemManager.register(new ItemBuildCraft_BC8("item.gear.gold"));
        gearDiamond = ItemManager.register(new ItemBuildCraft_BC8("item.gear.diamond"));
        guide = ItemManager.register(new ItemGuide("item.guide"));
        paintbrush = ItemManager.register(new ItemPaintbrush_BC8("item.paintbrush"));
        wrench = ItemManager.register(new ItemWrench_BC8("item.wrench"));
        list = ItemManager.register(new ItemList_BC8("item.list"));
        mapLocation = ItemManager.register(new ItemMapLocation("item.map_location"));
        markerConnector = ItemManager.register(new ItemMarkerConnector("item.marker_connector"));
    }
}
