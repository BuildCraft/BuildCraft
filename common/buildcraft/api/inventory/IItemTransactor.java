package buildcraft.api.inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.core.IStackFilter;

/** A simple way to define something that deals with item insertion and extraction, without caring about slots. */
public interface IItemTransactor {
    /** @param stack The stack to insert. Must not be null!
     * @param allOrNone If true then either the entire stack will be used or none of it.
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The overflow stack. Will be {@link ItemStack#EMPTY} if all of it was accepted. */
    @Nonnull
    ItemStack insert(@Nonnull ItemStack stack, boolean allOrNone, boolean simulate);

    /** Similar to {@link #insert(ItemStack, boolean, boolean)} but probably be more efficient at inserting lots of
     * items.
     * 
     * @param stacks The stacks to insert. Must not be null!
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The overflow stacks. Will be an empty list if all of it was accepted. */
    // TODO (Java 8) replace this with a default method that is ItemTransactorHelper.insertAllBypass
    NonNullList<ItemStack> insert(NonNullList<ItemStack> stacks, boolean simulate);

    /** Extracts a number of items that match the given filter
     * 
     * @param filter The filter that MUST be met by the extracted stack. Null means no filter - it can be any item.
     * @param min The minimum number of items to extract, or 0 if not enough items can be extracted
     * @param max The maximum number of items to extract.
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The stack that was extracted, or null if it could not be. */
    @Nonnull
    ItemStack extract(@Nullable IStackFilter filter, int min, int max, boolean simulate);
}
