/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.tileentity.TileEntity;

/**
 * @deprecated This has been replaced by the Pipe Event system.
 */
@Deprecated
public interface IItemTravelingHook {
	void drop(PipeTransportItems transport, TravelingItem item);

	void centerReached(PipeTransportItems transport, TravelingItem item);

	/**
	 * Overrides default handling of what occurs when an Item reaches the end of
	 * the pipe.
	 *
	 * @param transport
	 * @param item
	 * @param tile
	 * @return false if the transport code should handle the item normally, true
	 * if its been handled
	 */
	boolean endReached(PipeTransportItems transport, TravelingItem item, TileEntity tile);
}
