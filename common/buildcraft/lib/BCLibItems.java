package buildcraft.lib;

import buildcraft.lib.item.ItemGuide;
import buildcraft.lib.item.ItemManager;

public class BCLibItems {
    public static ItemGuide guide;

    private static boolean enableGuide;

    public static void enableGuide() {
        enableGuide = true;
    }

    public static void preInit() {
        if (enableGuide) {
            guide = ItemManager.register(new ItemGuide("item.guide"), true);
        }
    }
}
