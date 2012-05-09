/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicStone;
import net.minecraft.src.buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsStone extends Pipe {

	public PipeLiquidsStone(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicStone(), itemID);

//		((PipeTransportLiquids) transport).flowRate = 40;
	}

	@Override
	public int getMainBlockTexture() {
		return 7 * 16 + 2;
	}
}
