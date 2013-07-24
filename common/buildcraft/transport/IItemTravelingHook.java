package buildcraft.transport;

import net.minecraft.tileentity.TileEntity;

public interface IItemTravelingHook {

	public void drop(PipeTransportItems transport, EntityData data);

	public void centerReached(PipeTransportItems transport, EntityData data);

	public void endReached(PipeTransportItems transport, EntityData data, TileEntity tile);

}
