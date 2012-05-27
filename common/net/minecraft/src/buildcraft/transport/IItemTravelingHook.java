package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.TileEntity;

public interface IItemTravelingHook {

	public void drop (PipeTransportItems pipe, EntityData data);

	public void centerReached (PipeTransportItems pipe, EntityData data);

	public void endReached(PipeTransportItems pipe,	EntityData data, TileEntity tile);

}
