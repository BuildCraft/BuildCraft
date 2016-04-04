package a.buildcraft.core;

import a.buildcraft.lib.MigrationManager;
import a.buildcraft.lib.item.ItemBuildCraft_BC8;

import buildcraft.core.*;

public class BCCoreItems {
    public static ItemWrench wrench;
    public static ItemList list;
    public static ItemMapLocation mapLocation;
    public static ItemPaintbrush paintbrush;
    public static ItemBuildCraft_BC8 gearWood;
    public static ItemBuildCraft_BC8 gearStone;
    public static ItemBuildCraft_BC8 gearIron;
    public static ItemBuildCraft_BC8 gearGold;
    public static ItemBuildCraft_BC8 gearDiamond;

    public static void preInit() {
        wrench = registerWrench("wrench", "wrenchItem");
        list = registerList("list", "listItem");
        mapLocation = registerMapLocation("map_location", "mapLocation");
        paintbrush = registerPaintbrush("paintbrush", "paintbrush");
        gearWood = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.wood"));
        gearStone = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.stone"));
        gearIron = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.iron"));
        gearGold = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.gold"));
        gearDiamond = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.diamond"));

        MigrationManager.INSTANCE.addItemMigration(wrench, "wrenchItem");
        MigrationManager.INSTANCE.addItemMigration(list, "listItem");
        MigrationManager.INSTANCE.addItemMigration(mapLocation, "mapLocationItem");
    }

    @Deprecated
    private static ItemWrench registerWrench(String regName, String unlocalizedName) {
        ItemWrench wrench = new ItemWrench();
        wrench.setRegistryName("buildcraftcore", regName);
        wrench.setUnlocalizedName(unlocalizedName);
        if (BCRegistry.INSTANCE.registerItem(wrench, false)) {
            return wrench;
        }
        return null;
    }

    @Deprecated
    private static ItemList registerList(String regName, String unlocalizedName) {
        ItemList list = new ItemList();
        list.setRegistryName("buildcraftcore", regName);
        list.setUnlocalizedName(unlocalizedName);
        if (BCRegistry.INSTANCE.registerItem(list, false)) {
            return list;
        }
        return null;
    }

    @Deprecated
    private static ItemMapLocation registerMapLocation(String regName, String unlocalizedName) {
        ItemMapLocation map = new ItemMapLocation();
        map.setRegistryName("buildcraftcore", regName);
        map.setUnlocalizedName(unlocalizedName);
        if (BCRegistry.INSTANCE.registerItem(map, false)) {
            return map;
        }
        return null;
    }

    @Deprecated
    private static ItemPaintbrush registerPaintbrush(String regName, String unlocalizedName) {
        ItemPaintbrush paintbrush = new ItemPaintbrush();
        paintbrush.setRegistryName("buildcraftcore", regName);
        paintbrush.setUnlocalizedName(unlocalizedName);
        if (BCRegistry.INSTANCE.registerItem(paintbrush, false)) {
            return paintbrush;
        }
        return null;
    }
}
