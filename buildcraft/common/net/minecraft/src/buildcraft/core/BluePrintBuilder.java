package net.minecraft.src.buildcraft.core;

import net.minecraft.src.World;

public class BluePrintBuilder {
	
	public static enum Mode {Simple, Template}
	
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
		return findNextBlock(world, Mode.Simple);
	}
	
	public BlockContents findNextBlock (World world, Mode mode) {
		for (int j = 0; j < bluePrint.sizeY; ++j) {
			for (int i = 0; i < bluePrint.sizeX; ++i) {
				for (int k = 0; k < bluePrint.sizeZ; ++k) {										
					int xCoord = i + x - bluePrint.anchorX;
					int yCoord = j + y - bluePrint.anchorY;
					int zCoord = k + z - bluePrint.anchorZ;		
					
					int blockId = world.getBlockId (xCoord, yCoord, zCoord);
						
					BlockContents content = bluePrint.contents [i][j][k];
					
					if (content == null) {
						continue;
					}
					
					if (mode == Mode.Simple) {						
						if (blockId != content.blockId) {
							content = content.clone ();
							content.x = xCoord;
							content.y = yCoord;
							content.z = zCoord;

							return content;
						}
					} else if (mode == Mode.Template) {
						if ((content.blockId != 0 && blockId == 0)
								|| (content.blockId == 0 && blockId != 0)) {
							
							content = new BlockContents();
							content.x = xCoord;
							content.y = yCoord;
							content.z = zCoord;
							content.blockId = blockId;
							
							return content;
						}
					}
				}

			}
		}
		
		done = true;
		
		return null;
	}

}
