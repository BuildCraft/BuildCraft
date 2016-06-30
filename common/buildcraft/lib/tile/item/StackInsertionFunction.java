package buildcraft.lib.tile.item;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.BCLog;
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
            BCLog.logger.info("");
            BCLog.logger.info("Current " + addingTo + ", inserting " + toInsert);// debug

            if (toInsert == null) {
                BCLog.logger.info("nothing...");
                return new InsertionResult(addingTo, null);
            }

            if (addingTo == null) {
                int maxSize = Math.min(maxStackSize, toInsert.getMaxStackSize());
                if (toInsert.stackSize <= maxSize) {
                    BCLog.logger.info("correctSize");
                    return new InsertionResult(toInsert, null);
                } else {
                    ItemStack inserted = toInsert.splitStack(maxSize);
                    BCLog.logger.info("tooBig");
                    return new InsertionResult(inserted, toInsert);
                }
            } else if (addingTo.stackSize == maxStackSize) {
                BCLog.logger.info("tooBig");
                return new InsertionResult(addingTo, toInsert);
            } else if (StackUtil.canMerge(addingTo, toInsert)) {
                ItemStack complete = addingTo.copy();
                int count = addingTo.stackSize + toInsert.stackSize;
                int maxSize = Math.min(maxStackSize, complete.getMaxStackSize());
                if (count <= maxSize) {
                    complete.stackSize = count;
                    BCLog.logger.info("allInOne");
                    return new InsertionResult(complete, null);
                } else {
                    complete.stackSize = maxSize;
                    ItemStack leftOver = toInsert.copy();
                    leftOver.stackSize = count - maxSize;
                    BCLog.logger.info("someIn");
                    return new InsertionResult(complete, leftOver);
                }
            }
            BCLog.logger.info("overran");
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
            BCLog.logger.info("Setting to " + toSet + ", returning " + toReturn);// debug
        }
    }
}
