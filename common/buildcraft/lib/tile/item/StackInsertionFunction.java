package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.misc.StackUtil;

/** Defines a way of inserting items into an inventory - this can be overridden to have different rules with stack
 * merging. */
@FunctionalInterface
public interface StackInsertionFunction {
    /** @param addingTo The existing stack. Modifications are lost.
     * @param toInsert The stacks to insert. Modifications are lost.
     * @return The result of attempting to insert it. */
    @Nonnull
    InsertionResult modifyForInsertion(int slot, ItemStack addingTo, ItemStack toInsert);

    /** Gets a stack insertion function that will insert items up to a given stack size. The stack size of the items
     * themselves IS taken into account, so this has an effective upper limit of 64. */
    public static StackInsertionFunction getInsertionFunction(int maxStackSize) {
        return (slot, addingTo, toInsert) -> {
            if (toInsert == null) {
                return new InsertionResult(addingTo, null);
            }

            if (addingTo == null) {
                int maxSize = Math.min(maxStackSize, toInsert.getMaxStackSize());
                if (toInsert.stackSize <= maxSize) {
                    return new InsertionResult(toInsert, null);
                } else {
                    ItemStack inserted = toInsert.splitStack(maxSize);
                    return new InsertionResult(inserted, toInsert);
                }
            } else if (addingTo.stackSize == maxStackSize) {
                return new InsertionResult(addingTo, toInsert);
            } else if (StackUtil.canMerge(addingTo, toInsert)) {
                ItemStack complete = addingTo.copy();
                int count = addingTo.stackSize + toInsert.stackSize;
                int maxSize = complete.getMaxStackSize();
                if (count <= maxSize) {
                    complete.stackSize = count;
                    return new InsertionResult(complete, null);
                } else {
                    complete.stackSize = maxSize;
                    ItemStack leftOver = toInsert.copy();
                    leftOver.stackSize = count - maxSize;
                    return new InsertionResult(complete, leftOver);
                }
            }
            return new InsertionResult(addingTo, toInsert);
        };
    }

    /** Gets a stack insertion function that will insert up to full stacks into a given slot. This is just
     * {@link #getInsertionFunction(int)} with an argument of 64. */
    public static StackInsertionFunction getDefaultInserter() {
        return getInsertionFunction(64);
    }

    /** The result of an attempted insertion. */
    public static class InsertionResult {
        public static final InsertionResult NULL_STACKS = new InsertionResult(null, null);

        public final ItemStack toSet, toReturn;

        public InsertionResult(ItemStack toSet, ItemStack toReturn) {
            this.toSet = toSet;
            this.toReturn = toReturn;
        }
    }
}
