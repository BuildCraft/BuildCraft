package net.minecraft.src.buildcraft.transport.pipes;

import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicGold;
import net.minecraft.src.buildcraft.transport.PipeTransportPower;

public class PipePowerSnow extends Pipe {

	public PipePowerSnow(int itemID) {
		super(new PipeTransportPower(), new PipeLogicGold(), itemID);
		
		((PipeTransportPower) transport).powerResitance = 0;

	}

	public int getBlockTexture() {
		return 7 * 16 + 11;
	}

	
}
