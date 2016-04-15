package buildcraft.core;

import buildcraft.lib.item.ItemBuildCraft_BC8;

public class BCCoreItems {
    public static ItemBuildCraft_BC8 gearWood;
    public static ItemBuildCraft_BC8 gearStone;
    public static ItemBuildCraft_BC8 gearIron;
    public static ItemBuildCraft_BC8 gearGold;
    public static ItemBuildCraft_BC8 gearDiamond;
    public static ItemBuildCraft_BC8 guide;

    public static void preInit() {
        gearWood = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.wood"));
        gearStone = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.stone"));
        gearIron = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.iron"));
        gearGold = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.gold"));
        gearDiamond = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.gear.diamond"));
    }
}
