/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.TileEntity;

public class LegacyTile extends TileEntity {
	
	@Override
	public void updateEntity () {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		int pipeId = ((LegacyBlock) Block.blocksList[worldObj.getBlockId(
				xCoord, yCoord, zCoord)]).newPipeId;
		
		BlockGenericPipe.createPipe(xCoord, yCoord, zCoord,
				pipeId);
		worldObj.setBlockAndMetadataWithNotify(xCoord, yCoord, zCoord,
				BuildCraftTransport.genericPipeBlock.blockID, meta);
	}

}
