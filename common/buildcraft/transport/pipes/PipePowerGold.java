/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import buildcraft.api.core.Orientations;
import buildcraft.core.DefaultProps;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeLogicGold;
import buildcraft.transport.PipeTransportPower;

public class PipePowerGold extends Pipe {

	public PipePowerGold(int itemID) {
		super(new PipeTransportPower(), new PipeLogicGold(), itemID);

		((PipeTransportPower) transport).powerResitance = 0.001;
	}
	
	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		return 7 * 16 + 10;
	}


}
