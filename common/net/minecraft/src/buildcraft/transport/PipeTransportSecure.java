package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.pipes.IOwnable;
import net.minecraft.src.buildcraft.api.pipes.ISecuredInventory;
import net.minecraft.src.buildcraft.core.IMachine;

public class PipeTransportSecure extends PipeTransportSolids {
	
	@Override
	public boolean isPipeConnected(TileEntity tile) {
		
		if(tile instanceof IOwnable) {
			IOwnable ownable = (IOwnable)tile;
			if(ownable.isSecure())
				return ownable.getOwnerName().equals(container.getOwnerName());
		}
		
		if(tile instanceof ISecuredInventory) {
			ISecuredInventory inventory = (ISecuredInventory)tile;
			if(inventory.allowsInteraction(container.getOwnerName()))
				return true;
		}
		
		return false;
	}
	
	@Override
	public boolean allowsConnect(PipeTransport with) {
		
		if(with instanceof PipeTransportSecure)
			return true;
		
		return false;
	}

}
