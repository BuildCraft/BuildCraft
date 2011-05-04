package net.minecraft.src.buildcraft;

public class BluePrint {
	
	BlockContents contents [][][];
	
	public int sizeX, sizeY, sizeZ;
	
	public BluePrint (int sizeX, int sizeY, int sizeZ) {
		contents = new BlockContents [sizeX][sizeY][sizeZ];
		
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}
	
	public void setBlockId (int x, int y, int z, int blockId) {
		if (contents [x][y][z] == null) {
			contents [x][y][z] = new BlockContents ();
			contents [x][y][z].x = x;
			contents [x][y][z].y = y;
			contents [x][y][z].z = z;
		}
		
		contents [x][y][z].blockId = blockId;
	}
	
}
