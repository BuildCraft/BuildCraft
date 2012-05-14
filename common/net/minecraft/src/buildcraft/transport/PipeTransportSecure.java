package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.utils.IOwnable;

public class PipeTransportSecure extends PipeTransportItems {
	
	@Override
	public boolean isPipeConnected(TileEntity tile) {
		if(!(tile instanceof IOwnable))
			return false;
		
		IOwnable ownable = (IOwnable)tile;
		return ownable.getOwnerName().equals(container.getOwnerName());
	}
	
	@Override
	public boolean allowsConnect(PipeTransport with) {
		return with instanceof PipeTransportSecure;
	}

}
