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
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;

public class PipeLogicQuartz extends PipeLogic {

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		IPipe pipe2 = null;

		if (tile instanceof IPipeTile) {
			pipe2 = ((IPipeTile) tile).getPipe();
		}

		if (pipe2 != null) {
			if (pipe2.getLogic() instanceof PipeLogicStone) {
				return false;
			}

			if (pipe2.getLogic() instanceof PipeLogicCobblestone) {
				return false;
			}
		}

		return super.canPipeConnect(tile, side);
	}

}
