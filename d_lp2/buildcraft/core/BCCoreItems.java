package buildcraft.core;

import buildcraft.core.item.*;
import buildcraft.lib.item.ItemBuildCraft_BC8;

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
        gearWood = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.wood"));
        gearStone = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.stone"));
        gearIron = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.iron"));
        gearGold = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.gold"));
        gearDiamond = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.diamond"));
        guide = ItemBuildCraft_BC8.register(new ItemGuide("item.guide"));
        paintbrush = ItemBuildCraft_BC8.register(new ItemPaintbrush_BC8("item.paintbrush"));
        wrench = ItemBuildCraft_BC8.register(new ItemWrench_BC8("item.wrench"));
        list = ItemBuildCraft_BC8.register(new ItemList_BC8("item.list"));
        mapLocation = ItemBuildCraft_BC8.register(new ItemMapLocation("item.map_location"));
        markerConnector = ItemBuildCraft_BC8.register(new ItemMarkerConnector("item.marker_connector"));
    }
}
