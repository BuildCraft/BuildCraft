/** 
 * Copyright (c) SpaceToad, 2011
 * 
 * This file is part of the BuildCraft API. You have the rights to read, 
 * modify, compile or run this the code without restrictions. In addition, it
 * allowed to redistribute this API as well, either in source or binaries 
 * form, or to integrate it into an other mod.
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
