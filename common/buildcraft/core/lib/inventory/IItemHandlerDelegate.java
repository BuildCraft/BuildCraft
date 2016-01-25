package buildcraft.core.lib.inventory;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;

public interface IItemHandlerDelegate {
    public interface FunctionInsertItem {
        ItemStack insertItem(IItemHandler handler, int slot, ItemStack stack, boolean simulate);
    }

    public interface FunctionExtractItem {
        ItemStack extractItem(IItemHandler handler, int slot, int amount, boolean simulate);
    }

    public interface PredicateIsItemValidForSlot {
        boolean isItemValidForSlot(IItemHandler handler, int slot, ItemStack stack);

        default FunctionInsertItem toFuncInsertItem() {
            return new InsertItemChecker(this);
        }
    }

    public static class InsertItemChecker implements FunctionInsertItem {
        private final PredicateIsItemValidForSlot predicate;

        public InsertItemChecker(PredicateIsItemValidForSlot predicate) {
            this.predicate = predicate;
        }

        @Override
        public ItemStack insertItem(IItemHandler handler, int slot, ItemStack stack, boolean simulate) {
            if (!predicate.isItemValidForSlot(handler, slot, stack)) return stack;
            return handler.insertItem(slot, stack, simulate);
        }
    }
}
