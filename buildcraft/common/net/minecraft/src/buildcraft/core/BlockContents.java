/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
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