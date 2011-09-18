package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicStone;
import net.minecraft.src.buildcraft.transport.PipeTransportPower;

public class PipePowerStone extends Pipe {

	public PipePowerStone(int itemID) {
		super(new PipeTransportPower(), new PipeLogicStone(), itemID);
		
	}
	
	@Override
	public int getBlockTexture() {
		return 7 * 16 + 8;
	}


}
