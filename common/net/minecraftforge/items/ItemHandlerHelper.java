// STUB(R.Chen): Forge ItemHandlerHelper — compile shim.
package net.minecraftforge.items;

import net.minecraft.item.ItemStack;

public class ItemHandlerHelper {

    public static ItemStack insertItemStacked(IItemHandler dest, ItemStack stack, boolean simulate) {
        // TODO(R.Chen): implement via Fabric ItemStorage
        return stack;
    }

    public static ItemStack copyStackWithSize(ItemStack stack, int size) {
        if (stack.isEmpty() || size <= 0) return ItemStack.EMPTY;
        ItemStack copy = stack.copy();
        copy.setCount(size);
        return copy;
    }

    public static boolean canItemStacksStack(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ItemStack.areNbtEqual(a, b);
    }
}
