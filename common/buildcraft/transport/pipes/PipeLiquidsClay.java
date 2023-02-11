/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.DefaultProps;
import buildcraft.transport.IPipeTransportLiquidsFilterDirectionsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidStack;

import java.util.LinkedList;
import java.util.List;

public class PipeLiquidsClay extends Pipe implements IPipeTransportLiquidsFilterDirectionsHook {

	public PipeLiquidsClay(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicClay(), itemID);

		((PipeTransportLiquids) transport).flowRate = 20;
		((PipeTransportLiquids) transport).travelDelay = 6;
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		return 11 * 16 + 15;
	}

	@Override
	public List<ForgeDirection> filterPossibleMovements(List<ForgeDirection> possibleOrientations, Position pos, LiquidStack resource) {
		LinkedList<ForgeDirection> nonPipesList = new LinkedList<ForgeDirection>();
		LinkedList<ForgeDirection> pipesList = new LinkedList<ForgeDirection>();

		for (ForgeDirection o : possibleOrientations) {
			if (container.pipe.outputOpen(o)) {
				if (container.isPipeConnected(o)) {
					TileEntity entity = container.getTile(o);
					if (entity instanceof ITankContainer) {
						int filled = ((ITankContainer) entity).fill(o.getOpposite(), resource, false);
						if (filled > 0) {
							if (entity instanceof IPipeTile) {
								pipesList.add(o);
							} else {
								nonPipesList.add(o);
							}
						}
					}
				}
			}
		}

		return nonPipesList.isEmpty() ? pipesList : nonPipesList;
	}
}
