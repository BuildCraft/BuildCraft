package buildcraft.transport;

import net.minecraft.tileentity.TileEntity;

public interface IItemTravelingHook {

	public void drop(PipeTransportItems transport, TravelingItem item);

	public void centerReached(PipeTransportItems transport, TravelingItem item);

	public void endReached(PipeTransportItems transport, TravelingItem item, TileEntity tile);

}
