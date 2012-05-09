/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport.pipes;

import java.util.LinkedList;

import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.IPipeTransportItemsHook;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicGold;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;

public class PipeItemsGold extends Pipe implements IPipeTransportItemsHook {

	public PipeItemsGold(int itemID) {
		super(new PipeTransportItems(), new PipeLogicGold(), itemID);
	}

	@Override
	public int getMainBlockTexture() {
		if (worldObj != null
				&& worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord,
						zCoord))
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
			return (pipe2 == null || !(pipe2.logic instanceof PipeLogicGold))
					&& super.isPipeConnected(tile);
	}

	@Override
	public LinkedList<Orientations> filterPossibleMovements(
			LinkedList<Orientations> possibleOrientations, Position pos,
			EntityPassiveItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(EntityPassiveItem item, Orientations orientation) {
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
			item.speed = Utils.pipeNormalSpeed * 20F;
	}

	@Override
	public void readjustSpeed(EntityPassiveItem item) {
		((PipeTransportItems) transport).defaultReajustSpeed(item);
	}
}
