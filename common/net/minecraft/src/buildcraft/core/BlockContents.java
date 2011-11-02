/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.core;

public class BlockContents {
	public int blockId;
	public int x;
	public int y;
	public int z;
	
	public BlockContents clone () {
		BlockContents obj = new BlockContents();
		
		obj.x = x;
		obj.y = y;
		obj.z = z;
		obj.blockId = blockId;
		
		return obj;
	}
}