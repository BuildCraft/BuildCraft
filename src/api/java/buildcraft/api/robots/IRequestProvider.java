/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.robots;

import net.minecraft.item.ItemStack;

public interface IRequestProvider {
    /** Return the total number of request slots available from this provider. */
    int getNumberOfRequests();

    /** Return the stack requested in slot i, provided that this request is not in process of being provided by a
     * robot. */
    StackRequest getAvailableRequest(int i);

    /** Allocate the request at slot i to the robot given in parameter, and return true if the allocation is
     * successful. */
    boolean takeRequest(int i, EntityRobotBase robot);

    /** Provide a stack to fulfill request at index i. Return the stack minus items that have been taken. */
    ItemStack provideItemsForRequest(int i, ItemStack stack);
}
