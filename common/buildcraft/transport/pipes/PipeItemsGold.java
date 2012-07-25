/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import java.util.LinkedList;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.Utils;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeLogicGold;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

import net.minecraft.src.TileEntity;

public class PipeItemsGold extends Pipe implements IPipeTransportItemsHook {

	public PipeItemsGold(int itemID) {
		super(new PipeTransportItems(), new PipeLogicGold(), itemID);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		if (worldObj != null && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			return 1 * 16 + 14;
		else
			return 1 * 16 + 4;
	}
	
	@Override
	public boolean isPipeConnected(TileEntity tile) {
		if (!super.isPipeConnected(tile))
			return false;

		Pipe pipe2 = null;

		if (tile instanceof TileGenericPipe)
			pipe2 = ((TileGenericPipe) tile).pipe;

		if (BuildCraftTransport.alwaysConnectPipes)
			return super.isPipeConnected(tile);
		else
			return (pipe2 == null || !(pipe2.logic instanceof PipeLogicGold)) && super.isPipeConnected(tile);
	}

	@Override
	public LinkedList<Orientations> filterPossibleMovements(LinkedList<Orientations> possibleOrientations, Position pos,
			IPipedItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(IPipedItem item, Orientations orientation) {
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			item.setSpeed(Utils.pipeNormalSpeed * 20F);
	}

	@Override
	public void readjustSpeed(IPipedItem item) {
		((PipeTransportItems) transport).defaultReajustSpeed(item);
	}
}
