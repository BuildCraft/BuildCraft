/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License.
 */
package ct.buildcraft.api.robots;

import net.minecraft.world.item.ItemStack;

/** Provides item requests that need to be fulfilled by robots. */
public interface IRequestProvider {
    int getRequestsCount();

    /** Return the stack requested in the slot, or {@link ItemStack#EMPTY} when there is no request. */
    ItemStack getRequest(int slot);

    /** Fulfill the request in the slot and return any excess that was not used. */
    ItemStack offerItem(int slot, ItemStack stack);
}
