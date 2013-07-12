/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.pipes;

import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeLogicQuartz extends PipeLogic {

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		Pipe pipe2 = null;

		if (tile instanceof TileGenericPipe) {
			pipe2 = ((TileGenericPipe) tile).pipe;
		}

		if (pipe2 != null) {
			if (pipe2.logic instanceof PipeLogicStone) {
				return false;
			}

			if (pipe2.logic instanceof PipeLogicCobblestone) {
				return false;
			}
		}

		return super.canPipeConnect(tile, side);
	}

}
