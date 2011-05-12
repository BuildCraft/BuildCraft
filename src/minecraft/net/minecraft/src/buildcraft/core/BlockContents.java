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