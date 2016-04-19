package buildcraft.factory;

import buildcraft.lib.item.ItemBuildCraft_BC8;

public class BCFactoryItems {
    public static ItemBuildCraft_BC8 plasticSheet;

    public static void preInit() {
        plasticSheet = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.plastic.sheet"));
    }
}
