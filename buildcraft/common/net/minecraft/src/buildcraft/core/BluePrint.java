package net.minecraft.src.buildcraft.core;

public class BluePrint {
	
	BlockContents contents [][][];
	
	
	public int anchorX, anchorY, anchorZ;
	public int sizeX, sizeY, sizeZ;
	
	public BluePrint (BluePrint src) {
		anchorX = src.anchorX;
		anchorY = src.anchorY;
		anchorZ = src.anchorZ;
		
		sizeX = src.sizeX;
		sizeY = src.sizeY;
		sizeZ = src.sizeZ;
		
		contents = new BlockContents [sizeX][sizeY][sizeZ];
		
		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeZ; ++z) {
					contents [x][y][z] = src.contents [x][y][z];
				}
			}
		}
	}
	
	public BluePrint (int sizeX, int sizeY, int sizeZ) {
		contents = new BlockContents [sizeX][sizeY][sizeZ];
		
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		
		anchorX = 0;
		anchorY = 0;
		anchorZ = 0;
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
	
	public void rotateLeft () {
		BlockContents newContents [][][] = new BlockContents [sizeZ][sizeY][sizeX];
		
		for (int x = 0; x < sizeZ; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				for (int z = 0; z < sizeX; ++z) {
					newContents [x][y][z] = contents [z][y][(sizeZ - 1) - x];
				}
			}
		}
		
		int newAnchorX, newAnchorY, newAnchorZ;
		
		newAnchorX = (sizeZ - 1) - anchorZ;
		newAnchorY = anchorY;
		newAnchorZ = anchorX;
		
		contents = newContents;
		int tmp = sizeX;
		sizeX = sizeZ;
		sizeZ = tmp;				
		
		anchorX = newAnchorX;
		anchorY = newAnchorY;
		anchorZ = newAnchorZ;
	}
	
}
