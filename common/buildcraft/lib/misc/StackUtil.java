package buildcraft.lib.misc;

import net.minecraft.item.ItemStack;

public class StackUtil {
    public static boolean canMerge(ItemStack a, ItemStack b) {
        // Checks item, damage
        if (!ItemStack.areItemsEqual(a, b)) {
            return false;
        }
        if (a.getMaxStackSize() <= a.stackSize) {
            return false;
        }

        if (b.getMaxStackSize() <= b.stackSize) {
            return false;
        }
        // checks tags and caps
        return ItemStack.areItemStackTagsEqual(a, b);
    }
}
