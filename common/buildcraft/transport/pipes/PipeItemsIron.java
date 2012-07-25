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
import buildcraft.transport.PipeLogicIron;
import buildcraft.transport.PipeTransportItems;

public class PipeItemsIron extends Pipe {

	private int baseTexture = 1 * 16 + 2;
	private int plainTexture = 1 * 16 + 3;

	public PipeItemsIron(int itemID) {
		super(new PipeTransportItems(), new PipeLogicIron(), itemID);

		((PipeTransportItems) transport).allowBouncing = true;
	}
	
	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		if (direction == Orientations.Unknown)
			return baseTexture;
		else {
			int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

			if (metadata == direction.ordinal())
				return baseTexture;
			else
				return plainTexture;
		}
	}


}
