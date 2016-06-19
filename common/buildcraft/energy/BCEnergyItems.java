package buildcraft.energy;

import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemManager;

public class BCEnergyItems {
    public static ItemBC_Neptune globOfOil;

    public static void preInit() {
        globOfOil = ItemManager.register(new ItemBC_Neptune("item.glob.oil"));
    }
}
