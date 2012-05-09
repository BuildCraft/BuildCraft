/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
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

		BlockGenericPipe.createPipe(worldObj, xCoord, yCoord, zCoord,
				pipeId);
		worldObj.setBlockAndMetadataWithNotify(xCoord, yCoord, zCoord,
				BuildCraftTransport.genericPipeBlock.blockID, meta);
	}

}
