package buildcraft.transport;

import buildcraft.lib.item.ItemBuildCraft_BC8;

public class BCTransportItems {
    public static ItemBuildCraft_BC8 waterproof;

    public static void preInit() {
        waterproof = ItemBuildCraft_BC8.register(new ItemBuildCraft_BC8("item.waterproof"));
    }
}
