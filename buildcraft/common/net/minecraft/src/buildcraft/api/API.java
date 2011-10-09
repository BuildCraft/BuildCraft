/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 * 
 * As a special exception, this file is part of the BuildCraft API and is 
 * allowed to be redistributed, either in source or binaries form.
 */

package net.minecraft.src.buildcraft.api;

import java.util.LinkedList;

import net.minecraft.src.Block;
import net.minecraft.src.World;


public class API {

	public static LinkedList <LiquidData> liquids = new LinkedList <LiquidData> ();

	public static int getLiquidForBucket(int itemID) {
		for (LiquidData d : liquids) {
			if (d.filledBucketId == itemID) {
				return d.liquidId;
			}
		}
		
		return 0;
	}

	public static int getBucketForLiquid(int liquidId) {
		for (LiquidData d : liquids) {
			if (d.liquidId == liquidId) {
				return d.filledBucketId;
			}
		}
		
		return 0;
	}

	/**
	 * Return true if the block given in parameter is pass through (e.g. air,
	 * water...)
	 */
	public static boolean softBlock (int blockId) {
		return blockId == 0 
				|| blockId == Block.waterStill.blockID
				|| blockId == Block.waterMoving.blockID
				|| Block.blocksList [blockId] == null;
	}

	/**
	 * Return true if the block cannot be broken, typically bedrock and lava
	 */
	public static boolean unbreakableBlock (int blockId) {
		return blockId == Block.bedrock.blockID
			|| blockId == Block.lavaStill.blockID
			|| blockId == Block.lavaMoving.blockID;
	}

	public static void breakBlock(World world, int x, int y, int z) {
		int blockId = world.getBlockId(x, y, z);
		
		if (blockId != 0) {
			Block.blocksList[blockId].dropBlockAsItem(world, x, y, z,
					world.getBlockMetadata(x, y, z));
		}				
		
		world.setBlockWithNotify(x, y, z, 0);
	}

}
