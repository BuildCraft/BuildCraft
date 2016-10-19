package buildcraft.lib.inventory;

import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;

import buildcraft.core.lib.inventory.filters.StackFilter;
import buildcraft.lib.misc.StackUtil;

import gnu.trove.list.array.TIntArrayList;

/** Designates an {@link IItemTransactor} that is backed by a simple, static, array based inventory. */
public abstract class AbstractInvItemTransactor implements IItemTransactor {
    /** Safely copies the input item stack, returning null if the stack size is less than or equal to zero. */
    public static ItemStack asValid(ItemStack stack) {
        if (stack == null) return null;
        if (stack.stackSize <= 0) return null;
        return stack;
    }

    protected abstract ItemStack insert(int slot, ItemStack stack, boolean simulate);

    protected abstract ItemStack extract(int slot, IStackFilter filter, int min, int max, boolean simulate);

    protected abstract int getSlots();

    protected abstract boolean isEmpty(int slot);

    @Override
    public ItemStack insert(ItemStack stack, boolean allAtOnce, boolean simulate) {
        if (allAtOnce) {
            return insertAllAtOnce(stack, simulate);
        } else {
            return insertAnyAmount(stack, simulate);
        }
    }

    private ItemStack insertAnyAmount(ItemStack stack, boolean simulate) {
        int slotCount = getSlots();
        TIntArrayList emptySlots = new TIntArrayList(slotCount);
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, simulate);
                if (stack == null) return null;
            }
        }
        for (int slot : emptySlots.toArray()) {
            stack = insert(slot, stack, simulate);
            if (stack == null) return null;
        }
        return stack;
    }

    private ItemStack insertAllAtOnce(ItemStack stack, boolean simulate) {
        ItemStack before = asValid(stack);
        TIntArrayList insertedSlots = new TIntArrayList(getSlots());
        TIntArrayList emptySlots = new TIntArrayList(getSlots());
        for (int slot = 0; slot < getSlots(); slot++) {
            if (isEmpty(slot)) {
                emptySlots.add(slot);
            } else {
                stack = insert(slot, stack, true);
                insertedSlots.add(slot);
                if (stack == null) break;
            }
        }
        for (int slot : emptySlots.toArray()) {
            stack = insert(slot, stack, true);
            insertedSlots.add(slot);
            if (stack == null) break;
        }
        if (stack != null) {
            return stack;
        }
        if (simulate) return null;
        for (int slot : insertedSlots.toArray()) {
            before = insert(slot, before, false);
        }
        if (before != null) {
            // We have a bad implemtation that doesn't respect simulation properly- we are in an invalid state at this
            // point with no chance of recovery
            throw new IllegalStateException("Somehow inserting a lot of items at once failed when we thought it shouldn't! (" + getClass() + ")");
        }
        return null;
    }

    @Override
    public List<ItemStack> insert(List<ItemStack> stacks, boolean simulate) {
        // WRANING: SLOW IMPL
        return stacks;
    }

    @Override
    public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
        if (min < 1) min = 1;
        if (min > max) return null;
        if (max < 0) return null;

        if (filter == null) {
            filter = StackFilter.ALL;
        }

        int slots = getSlots();
        TIntArrayList valids = new TIntArrayList();
        int totalSize = 0;
        ItemStack toExtract = null;

        for (int slot = 0; slot < slots; slot++) {
            ItemStack possible = extract(slot, filter, 1, max - totalSize, true);
            if (possible != null) {
                if (toExtract == null) {
                    toExtract = possible.copy();
                }
                if (StackUtil.canMerge(toExtract, possible)) {
                    totalSize += possible.stackSize;
                    valids.add(slot);
                    if (totalSize >= max) {
                        break;
                    }
                }
            }
        }

        ItemStack total = null;
        if (min <= totalSize) {
            for (int slot : valids.toArray()) {
                ItemStack extracted = extract(slot, filter, 1, max - (total == null ? 0 : total.stackSize), simulate);
                if (total == null) {
                    total = extracted;
                } else {
                    total.stackSize += extracted.stackSize;
                }
            }
        }
        return total;
    }
}
