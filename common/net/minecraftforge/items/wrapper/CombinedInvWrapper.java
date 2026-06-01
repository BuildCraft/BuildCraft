// STUB(R.Chen): Forge CombinedInvWrapper — compile shim.
package net.minecraftforge.items.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class CombinedInvWrapper implements IItemHandler {
    private final IItemHandler[] handlers;

    public CombinedInvWrapper(IItemHandler... handlers) { this.handlers = handlers; }

    @Override public int getSlots() { int s = 0; for (IItemHandler h : handlers) s += h.getSlots(); return s; }
    @Override public ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
    @Override public int getSlotLimit(int slot) { return 64; }
}
