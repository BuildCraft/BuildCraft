package buildcraft.api.inventory;

import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;

/** A simple way to define something that deals with item insertion and extraction, without caring about slots. */
public interface IItemTransactor {
    /** @param stack The stack to insert. Must not be null!
     * @param allOrNone If true then either the entire stack will be used or none of it.
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The overflow stack. Will be null if all of it was accepted. */
    ItemStack insert(ItemStack stack, boolean allOrNone, boolean simulate);

    /** Similar to {@link #insert(ItemStack, boolean, boolean)} but probably be more efficient at inserting lots of
     * items.
     * 
     * @param stacks The stacks to insert. Must not be null!
     * @param allOrNone If true then either all of the stacks will be completely inserted, or none at all.
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The overflow stacks. Will be an empty list if all of it was accepted. */
    List<ItemStack> insertAll(List<ItemStack> stacks, boolean simulate);

    /** Extracts a number of items that match the given filter
     * 
     * @param filter
     * @param min The minimum number of items to extract, or 0 if not enough items can be extracted
     * @param max The maximum number of items to extract.
     * @param simulate If true then the in-world state of this will not be changed.
     * @return The stack that was extracted, or null if it could not be. */
    ItemStack extract(IStackFilter filter, int min, int max, boolean simulate);
}
