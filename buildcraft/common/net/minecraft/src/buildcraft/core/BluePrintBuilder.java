package net.minecraft.src.buildcraft.core;

import net.minecraft.src.World;

public class BluePrintBuilder {
	
	public BluePrint bluePrint;
	int x, y, z;
	public boolean done;
	
	public BluePrintBuilder (BluePrint bluePrint, int x, int y, int z) {
		this.bluePrint = bluePrint;
		this.x = x;
		this.y = y;
		this.z = z;
		done = false;
	}
	
	public BlockContents findNextBlock (World world) {
		for (int j = 0; j < bluePrint.sizeY; ++j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {
					int blockId = world.getBlockId(i + x, j + y, k + z);
					
					
					BlockContents content = bluePrint.contents [i][j][k];
					
					if (content != null && blockId != content.blockId) {
						content = content.clone ();
						content.x = content.x + x;
						content.y = content.y + y;
						content.z = content.z + z;
						
						return content;
					}
				}

			}
		}
		
		done = true;
		
		return null;
	}

}
