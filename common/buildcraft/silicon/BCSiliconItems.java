package buildcraft.silicon;

import buildcraft.lib.item.ItemManager;
import buildcraft.silicon.item.ItemRedstoneChipset;

public class BCSiliconItems {
    public static ItemRedstoneChipset redstoneChipset;

    public static void preInit() {
        redstoneChipset = ItemManager.register(new ItemRedstoneChipset("item.redstone_chipset"));
    }
}
