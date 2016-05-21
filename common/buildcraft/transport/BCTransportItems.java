package buildcraft.transport;

import buildcraft.lib.item.ItemBuildCraft_BC8;
import buildcraft.lib.item.ItemManager;

public class BCTransportItems {
    public static ItemBuildCraft_BC8 waterproof;

    public static void preInit() {
        waterproof = ItemManager.register(new ItemBuildCraft_BC8("item.waterproof"));
    }
}
