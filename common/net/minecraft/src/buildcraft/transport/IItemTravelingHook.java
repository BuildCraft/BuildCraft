package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.TileEntity;

public interface IItemTravelingHook {

	public void drop (PipeTransportSolids pipe, EntityData data);

	public void centerReached (PipeTransportSolids pipe, EntityData data);

	public void endReached(PipeTransportSolids pipe,	EntityData data, TileEntity tile);

}
