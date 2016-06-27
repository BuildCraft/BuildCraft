package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandlerModifiable;

public class DelegateItemHandler implements IItemHandlerModifiable {
    private final IItemHandlerModifiable delegate;

    public DelegateItemHandler(IItemHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        delegate.setStackInSlot(slot, stack);
    }
}
