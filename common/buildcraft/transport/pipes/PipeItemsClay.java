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
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

import java.util.LinkedList;

public class PipeItemsClay extends Pipe implements IPipeTransportItemsHook {

	public PipeItemsClay(int itemID) {
		super(new PipeTransportItems(), new PipeLogicClay(), itemID);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		return 10 * 16 + 15;
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, IPipedItem item) {
		LinkedList<ForgeDirection> nonPipesList = new LinkedList<ForgeDirection>();
		LinkedList<ForgeDirection> pipesList = new LinkedList<ForgeDirection>();

		for (ForgeDirection o : possibleOrientations) {
			if (container.pipe.outputOpen(o)) {
				if (container.isPipeConnected(o)) {
					TileEntity entity = container.getTile(o);
					if (entity instanceof IPipeTile) {
						pipesList.add(o);
					} else {
						nonPipesList.add(o);
					}
				}
			}
		}

		return nonPipesList.isEmpty() ? pipesList : nonPipesList;
	}

	@Override
	public void entityEntered(IPipedItem item, ForgeDirection orientation) {

	}

	@Override
	public void readjustSpeed(IPipedItem item) {

	}
}
