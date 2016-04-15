package buildcraft.lib;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class CreativeTabManager {
    private static final Map<String, CreativeTabBC> tabMap = new HashMap<>();

    public static CreativeTabs getTab(String name) {
        if (tabMap.containsKey(name)) {
            return tabMap.get(name);
        } else {
            throw new IllegalArgumentException("Unknown tab " + name);
        }
    }

    public static void createTab(String name) {
        tabMap.put(name, new CreativeTabBC(name));
    }

    public static void setItem(String name, Item item) {
        if (item != null) {
            tabMap.get(name).item = item;
        }
    }

    private static class CreativeTabBC extends CreativeTabs {
        private Item item = Items.COMPARATOR; // Temp.

        public CreativeTabBC(String name) {
            super(name);
        }

        @Override
        public Item getTabIconItem() {
            return item;
        }
    }
}
