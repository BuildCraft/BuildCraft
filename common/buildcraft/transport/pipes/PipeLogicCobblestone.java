/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import net.minecraft.tileentity.TileEntity;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class PipeLogicCobblestone extends PipeLogic {

	@Override
	public boolean isPipeConnected(TileEntity tile) {
		Pipe pipe2 = null;

		if (tile instanceof TileGenericPipe) {
			pipe2 = ((TileGenericPipe) tile).pipe;
		}

		if (BuildCraftTransport.alwaysConnectPipes)
			return super.isPipeConnected(tile);
		else
			return (pipe2 == null || !(pipe2.logic instanceof PipeLogicStone)) && super.isPipeConnected(tile);
	}

}
