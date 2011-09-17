package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.TileEntity;

public class PipeLogicObsidian extends PipeLogic {

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		Pipe pipe2 = null;

		if (tile instanceof TileGenericPipe) {
			pipe2 = ((TileGenericPipe) tile).pipe;
		}

		if (BuildCraftTransport.alwaysConnectPipes) {
			return super.isPipeConnected(tile);
		} else {
			return (pipe2 == null || !(pipe2.logic instanceof PipeLogicObsidian))
					&& super.isPipeConnected(tile);
		}		
	}
	
}
