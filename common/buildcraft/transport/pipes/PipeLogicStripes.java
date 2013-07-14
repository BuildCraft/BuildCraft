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

public class PipeLogicStripes extends PipeLogic {

	@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		IPipe pipe2 = null;

		if (tile instanceof IPipeTile) {
			pipe2 = ((IPipeTile) tile).getPipe();
		}

		return (pipe2 == null || !(pipe2.getLogic() instanceof PipeLogicStripes) && !(pipe2.getLogic() instanceof PipeLogicObsidian)) && super.canPipeConnect(tile, side);
	}

}
