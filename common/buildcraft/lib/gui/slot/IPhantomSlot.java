/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.lib.gui.slot;

import net.minecraft.item.ItemStack;

/** Phantom Slots don't "use" items, they are used for filters and various other logic slots. */
public interface IPhantomSlot {
    /** @return True if this slot can change {@link ItemStack#setCount(int)} to a count other than 0 (empty) or 1
     *         (filled), false to limit this slot to only empty or have a count of 1. */
    boolean canAdjustCount();
}
