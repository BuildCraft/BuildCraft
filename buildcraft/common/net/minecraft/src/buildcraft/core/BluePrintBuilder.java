package net.minecraft.src.buildcraft.core;

import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.IBox;

public class BluePrintBuilder implements IAreaProvider {
	
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

	@Override
	public int xMin() {
		return x - bluePrint.anchorX;
	}

	@Override
	public int yMin() {
		return y - bluePrint.anchorY;
	}

	@Override
	public int zMin() {
		return z - bluePrint.anchorZ;
	}

	@Override
	public int xMax() {
		return x + bluePrint.sizeX - bluePrint.anchorX - 1;
	}

	@Override
	public int yMax() {
		return y + bluePrint.sizeY - bluePrint.anchorY - 1;
	}

	@Override
	public int zMax() {
		return z + bluePrint.sizeZ - bluePrint.anchorZ - 1;
	}

	@Override
	public void removeFromWorld() {
		
	}

	@Override
	public IBox getBox() {
		return new Box(this);
	}

}
