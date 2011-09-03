package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicStone;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsStone extends Pipe {

	public PipeItemsStone(int itemID) {
		super(new PipeTransportItems(), new PipeLogicStone (), itemID);
		
	}
	
	@Override
	public int getBlockTexture() {
		return 1 * 16 + 13;
	}

}
