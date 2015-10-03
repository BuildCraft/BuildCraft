/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.utils;

import net.minecraft.item.ItemStack;

import buildcraft.core.CoreConstants;

public final class TransportUtils {

    /** Deactivate constructor */
    private TransportUtils() {}

    /** Depending on the kind of item in the pipe, set the floor at a different level to optimize graphical aspect. */
    // Right. Depending on the item in the pipe. Because this function somehow does that with a single line.
    public static float getPipeFloorOf(ItemStack item) {
        return CoreConstants.PIPE_MIN_POS;
    }
}
