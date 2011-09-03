package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicWood;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

public class PipeItemsWood extends Pipe {
	
	int baseTexture = 1 * 16 + 0;
	int plainTexture = 1 * 16 + 15;

	public PipeItemsWood(int itemID) {
		super(new PipeTransportItems(), new PipeLogicWood(), itemID);
	}
	
	public int getBlockTexture() {
		return baseTexture;
	}
	
	@Override
	public boolean isPipeConnected(TileEntity tile) {
		Pipe pipe2 = null;
		
		if (tile instanceof TileGenericPipe) {
			pipe2 = ((TileGenericPipe) tile).pipe;
		}
		
		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(tile);
		} else {
			return (pipe2 == null || !(pipe2.logic instanceof PipeLogicWood))
					&& super.isPipeConnected(tile);
		}
	}

}
