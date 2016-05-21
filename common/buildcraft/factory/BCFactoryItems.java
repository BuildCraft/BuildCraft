package buildcraft.factory;

import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.lib.item.ItemManager;

public class BCFactoryItems {
    public static ItemBuildCraft_BC8 plasticSheet;

    public static void preInit() {
        plasticSheet = ItemManager.register(new ItemBuildCraft_BC8("item.plastic.sheet"));
    }
}
