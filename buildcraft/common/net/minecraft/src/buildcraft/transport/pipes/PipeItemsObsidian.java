package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicObsidian;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsObsidian extends Pipe {

	public PipeItemsObsidian(int itemID) {
		super(new PipeTransportItems(), new PipeLogicObsidian(), itemID);
	}
	
	public int getBlockTexture() {
		return 1 * 16 + 12;
	}

}
