package buildcraft.lib.inventory;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

import buildcraft.api.core.IStackFilter;

public final class ItemHandlerWrapper extends AbstractInvItemTransactor {
    private final IItemHandler wrapped;

    public ItemHandlerWrapper(IItemHandler handler) {
        this.wrapped = handler;
    }

    @Override
    protected ItemStack insert(int slot, ItemStack stack, boolean simulate) {
        return wrapped.insertItem(slot, stack, simulate);
    }

    @Override
    protected ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate) {
        if (max < 0 || max < min) return null;
        ItemStack current = wrapped.getStackInSlot(slot);
        if (current == null || current.stackSize < min) return null;
        if (filter.matches(safeCopy(current))) {
            return wrapped.extractItem(slot, max, simulate);
        }
        return null;
    }

    @Override
    protected int getSlots() {
        return wrapped.getSlots();
    }

    @Override
    protected boolean isEmpty(int slot) {
        return wrapped.getStackInSlot(slot) == null;
    }
}