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
