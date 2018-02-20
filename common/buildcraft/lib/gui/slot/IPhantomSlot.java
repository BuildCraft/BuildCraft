/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

/** Phantom Slots don't "use" items, they are used for filters and various other logic slots. */
public interface IPhantomSlot {
    /** @return True if this slot can change ItemStack#stackSize to a count other than 0 (empty) or 1
     *         (filled), false to limit this slot to only empty or have a count of 1. */
    boolean canAdjustCount();
}
