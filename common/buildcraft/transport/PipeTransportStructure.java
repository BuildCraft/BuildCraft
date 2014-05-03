/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.transport.IPipeTile.PipeType;

public class PipeTransportStructure extends PipeTransport {

	@Override
	public PipeType getPipeType() {
		return PipeType.STRUCTURE;
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		if (tile instanceof TileGenericPipe) {
			Pipe pipe2 = ((TileGenericPipe) tile).pipe;

			if (BlockGenericPipe.isValid(pipe2) && !(pipe2.transport instanceof PipeTransportStructure)) {
				return false;
			}
		}

		return tile instanceof TileGenericPipe;
	}
}
