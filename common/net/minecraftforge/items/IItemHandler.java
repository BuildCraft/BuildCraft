// STUB(R.Chen): Forge IItemHandler — compile shim. TODO: migrate to Fabric Storage<ItemVariant>.
package net.minecraftforge.items;

import net.minecraft.item.ItemStack;

public interface IItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
}
