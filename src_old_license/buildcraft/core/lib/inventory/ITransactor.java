/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.api.core.IStackFilter;

@Deprecated
public interface ITransactor {

    /** Adds an Item to the inventory.
     *
     * @param stack
     * @param doAdd
     * @return The left over {@link ItemStack}. */
    ItemStack insert(ItemStack stack, boolean doAdd);

    /** Adds a list of {@link ItemStack}s to the inventory
     * 
     * @param stacks
     * @param doAdd
     * @return A list of all {@link ItemStack}s left over.
     * 
     * @implNote The default implementation works best for "opaque" inventories (i.e. ones that have been wrapped) but
     *           most classes can provide a faster implementation that takes advantage of that we are not removing any
     *           elements in between insertions of these items. */
    @Nonnull
    default List<ItemStack> insertAll(List<ItemStack> stacks, boolean doAdd) {
        List<ItemStack> leftOvers = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack leftOver = insert(stack, doAdd);
            if (leftOver != null && leftOver.stackSize > 0) leftOvers.add(leftOver);
        }
        return leftOvers;
    }

    /** Removes and returns a single item from the inventory matching the filter.
     * 
     * @param filter
     * @param doRemove
     * @return The {@link ItemStack} that has been removed. */
    ItemStack remove(IStackFilter filter, boolean doRemove);
}
