package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.PipeTransportSecure;
import net.minecraft.src.buildcraft.transport.PipeTransportSolids;

public class PipeItemsRedstone extends PipeItemsWood {

	public PipeItemsRedstone(int itemID) {
		super(itemID, new PipeTransportSecure());
	}

}
