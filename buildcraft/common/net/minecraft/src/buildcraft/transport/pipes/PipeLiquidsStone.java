package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicStone;
import net.minecraft.src.buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsStone extends Pipe {

	public PipeLiquidsStone(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicStone(), itemID);

	}

	@Override
	public int getBlockTexture() {
		return 7 * 16 + 2;
	}
}
