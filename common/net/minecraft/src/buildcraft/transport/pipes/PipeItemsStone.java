/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport.pipes;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.IPipeTransportItemsHook;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicStone;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsStone extends Pipe implements IPipeTransportItemsHook {

	public PipeItemsStone(int itemID) {
		super(new PipeTransportItems(), new PipeLogicStone(), itemID);

	}

	@Override
	public int getMainBlockTexture() {
		return 1 * 16 + 13;
	}

	@Override
	public void readjustSpeed(EntityPassiveItem item) {
		if (item.speed > Utils.pipeNormalSpeed)
			item.speed = item.speed - Utils.pipeNormalSpeed / 2.0F;

		if (item.speed < Utils.pipeNormalSpeed)
			item.speed = Utils.pipeNormalSpeed;
	}

	@Override
	public LinkedList<Orientations> filterPossibleMovements(LinkedList<Orientations> possibleOrientations, Position pos,
			EntityPassiveItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(EntityPassiveItem item, Orientations orientation) {

	}

}
