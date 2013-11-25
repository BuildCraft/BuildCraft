package buildcraft.transport;

import net.minecraft.tileentity.TileEntity;

/**
 * @deprecated This has been replaced by the Pipe Event system.
 */
@Deprecated
public interface IItemTravelingHook {

	public void drop(PipeTransportItems transport, TravelingItem item);

	public void centerReached(PipeTransportItems transport, TravelingItem item);

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
	public boolean endReached(PipeTransportItems transport, TravelingItem item, TileEntity tile);
}
