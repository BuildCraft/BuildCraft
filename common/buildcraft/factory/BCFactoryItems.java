package buildcraft.factory;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;

public class BCFactoryItems {
    public static ItemBC_Neptune plasticSheet;

    public static void preInit() {
        plasticSheet = ItemManager.register(new ItemBC_Neptune("item.plastic.sheet"));
    }
}
