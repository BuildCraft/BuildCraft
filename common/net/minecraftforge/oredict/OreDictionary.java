// STUB(R.Chen): Forge OreDictionary — compile shim. TODO: replace with vanilla Tags.
package net.minecraftforge.oredict;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public class OreDictionary {
    public static final int WILDCARD_VALUE = Integer.MAX_VALUE;

    public static void registerOre(String oreName, net.minecraft.block.Block ore) {
        // no-op — replaced by Tags in data/
    }

    public static void registerOre(String oreName, ItemStack ore) {
        // no-op — replaced by Tags in data/
    }

    public static List<ItemStack> getOres(String oreName) {
        return new ArrayList<>();
    }

    public static boolean doesOreNameExist(String oreName) {
        return false;
    }

    public static String[] getOreNames() {
        return new String[0];
    }
}
