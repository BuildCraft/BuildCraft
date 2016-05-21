package buildcraft.lib.tile.item;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface StackInsertionFunction {
    /** @param addingTo The existing stack. Modifications are lost.
     * @param toInsert The stacks to insert. Modifications are lost.
     * @return The result of attempting to insert it. */
    InsertionResult modifyForInsertion(int slot, ItemStack addingTo, ItemStack toInsert);

    public static StackInsertionFunction getInsertionFunction(int maxStackSize) {
        return (slot, addingTo, toInsert) -> {
            if (addingTo == null) {
                if (toInsert == null) {
                    // um, ok then
                    return InsertionResult.NULL_STACKS;
                } else {
                    int maxSize = Math.min(maxStackSize, toInsert.getMaxStackSize());
                    if (toInsert.stackSize <= maxSize) {
                        return new InsertionResult(toInsert, null);
                    } else {
                        ItemStack inserted = toInsert.splitStack(maxSize);
                        return new InsertionResult(inserted, toInsert);
                    }
                }
            } else {
                // TODO: addingTo is non-null
            }
            return null;
        };
    }

    public static StackInsertionFunction getDefaultInserter() {
        return getInsertionFunction(64);
    }

    public static class InsertionResult {
        public static final InsertionResult NULL_STACKS = new InsertionResult(null, null);

        public final ItemStack toSet, toReturn;

        public InsertionResult(ItemStack toSet, ItemStack toReturn) {
            this.toSet = toSet;
            this.toReturn = toReturn;
        }
    }
}
