/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.inventory;

import buildcraft.api.core.IInvSlot;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraftforge.items.IItemHandler;

public final class InventoryIterator {

    /** Deactivate constructor */
    private InventoryIterator() {}

    public static Iterable<IInvSlot> getIterable(Container inv) {
        return getIterable(inv, null);
    }

    public static Iterable<IInvSlot> getIterable(IItemHandler inv) {
        return getIterable(inv, null);
    }

    /** Returns an Iterable object for the specified side of the inventory.
     *
     * @param inv
     * @param side
     * @return Iterable */
    public static Iterable<IInvSlot> getIterable(Container inv, Direction side) {
        // if (inv instanceof ISidedInventory)
        if (inv instanceof WorldlyContainer) {
            // return new InventoryIteratorSided((ISidedInventory) inv, side);
            return new InventoryIteratorSided((WorldlyContainer) inv, side);
        }

        return new InventoryIteratorSimple(inv);
    }

    public static Iterable<IInvSlot> getIterable(IItemHandler inv, Direction side) {
        return new ItemHandlerIterator(inv);
    }

}
