/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.core.lib.inventory.filters.IStackFilter;

public interface ITransactor {

    /** Adds an Item to the inventory.
     *
     * @param stack
     * @param orientation
     * @param doAdd
     * @return The ItemStack, with stackSize equal to amount moved. */
    ItemStack add(ItemStack stack, EnumFacing orientation, boolean doAdd);

    /** Removes and returns a single item from the inventory matching the filter.
     * 
     * @param filter
     * @param orientation
     * @param doRemove */
    ItemStack remove(IStackFilter filter, EnumFacing orientation, boolean doRemove);
}
