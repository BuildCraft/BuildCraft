/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.core.DefaultProps;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportLiquids;

public class PipeLiquidsIron extends Pipe {

	private int baseTexture = 7 * 16 + 3;
	private int plainTexture = 1 * 16 + 3;

	public PipeLiquidsIron(int itemID) {
		super(new PipeTransportLiquids(), new PipeLogicIron(), itemID);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public int getTextureIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return baseTexture;
		else {
			if (((PipeLogicIron) logic).direction == direction)
				return baseTexture;
			else
				return plainTexture;
		}
	}

	@Override
	public boolean canConnectRedstone() {
		return true;
	}
}
