package buildcraft.transport;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;

public class BCTransportItems {
    public static ItemBC_Neptune waterproof;

    public static void preInit() {
        waterproof = ItemManager.register(new ItemBC_Neptune("item.waterproof"));
    }
}
