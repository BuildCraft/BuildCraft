/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import java.util.LinkedList;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;

public class PipeItemsStone extends Pipe implements IPipeTransportItemsHook {

	public PipeItemsStone(int itemID) {
		super(new PipeTransportItems(), new PipeLogicStone(), itemID);

	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		return 1 * 16 + 13;
	}

	@Override
	public void readjustSpeed(IPipedItem item) {
		if (item.getSpeed() > Utils.pipeNormalSpeed) {
			item.setSpeed(item.getSpeed() - Utils.pipeNormalSpeed / 2.0F);
		}

		if (item.getSpeed() < Utils.pipeNormalSpeed) {
			item.setSpeed(Utils.pipeNormalSpeed);
		}
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, IPipedItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(IPipedItem item, ForgeDirection orientation) {

	}

}
