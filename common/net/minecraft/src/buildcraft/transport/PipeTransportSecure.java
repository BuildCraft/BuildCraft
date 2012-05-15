package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.core.IMachine;
import net.minecraft.src.buildcraft.core.utils.IOwnable;
import net.minecraft.src.buildcraft.core.utils.ISecuredInventory;

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
			return inventory.getOwnerName().equals(container.getOwnerName());
		}
		
		return false;
	}
	
	@Override
	public boolean allowsConnect(PipeTransport with) {
		
		if(with instanceof PipeTransportSecure)
			return true;
		
		if(with.container.pipe.logic instanceof PipeLogicWood)
			return true;
		
		return false;
	}

}
