package buildcraft.core.lib.inventory;

import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

public class ItemHandlerDelegate implements IItemHandler {
    private final IItemHandler delegate;

    private ToIntFunction<IItemHandler> funcGetSlots;
    private BiFunction<IItemHandler, Integer, ItemStack> funcGetStackInSlot;
    private IItemHandlerDelegate.FunctionInsertItem funcInsertItem;
    private IItemHandlerDelegate.FunctionExtractItem funcExtractItem;

    private ItemHandlerDelegate(IItemHandler delegate) {
        this.delegate = delegate;
    }

    public static ItemHandlerDelegate createFrom(IItemHandler handler) {
        if (handler instanceof ItemHandlerDelegate) return (ItemHandlerDelegate) handler;
        return new ItemHandlerDelegate(handler);
    }

    public static ItemHandlerDelegate createFrom(IItemHandler handler, ToIntFunction<IItemHandler> funcGetSlots) {
        ItemHandlerDelegate delegate = createFrom(handler);
        delegate.funcGetSlots = funcGetSlots;
        return delegate;
    }

    public static ItemHandlerDelegate createFrom(IItemHandler handler, BiFunction<IItemHandler, Integer, ItemStack> funcGetStackInSlot) {
        ItemHandlerDelegate delegate = createFrom(handler);
        delegate.funcGetStackInSlot = funcGetStackInSlot;
        return delegate;
    }

    public static ItemHandlerDelegate createFrom(IItemHandler handler, IItemHandlerDelegate.FunctionInsertItem funcInsertItem) {
        ItemHandlerDelegate delegate = createFrom(handler);
        delegate.funcInsertItem = funcInsertItem;
        return delegate;
    }

    public static ItemHandlerDelegate createFrom(IItemHandler handler, IItemHandlerDelegate.FunctionExtractItem funcExtractItem) {
        ItemHandlerDelegate delegate = createFrom(handler);
        delegate.funcExtractItem = funcExtractItem;
        return delegate;
    }

    public static ItemHandlerDelegate createFrom(IItemHandler handler, IItemHandlerDelegate.PredicateIsItemValidForSlot predicate) {
        return createFrom(handler, predicate.toFuncInsertItem());
    }

    @Override
    public int getSlots() {
        if (funcGetSlots == null) return delegate.getSlots();
        return funcGetSlots.applyAsInt(delegate);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (funcGetStackInSlot == null) return delegate.getStackInSlot(slot);
        return funcGetStackInSlot.apply(delegate, slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (funcInsertItem == null) return delegate.insertItem(slot, stack, simulate);
        return funcInsertItem.insertItem(delegate, slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (funcExtractItem == null) return delegate.extractItem(slot, amount, simulate);
        return funcExtractItem.extractItem(delegate, slot, amount, simulate);
    }
}
