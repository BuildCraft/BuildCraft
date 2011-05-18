package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.core.EntityBlock;

public class TileMarker extends TileEntity {
	
	private static int maxSize = 50;
	
	public class Origin {
		TileMarker vectO;
		TileMarker [] vect = new TileMarker [3];	
	}
	
	Origin origin;
	
	EntityBlock [] lasers; 
	
	public void updateEntity() {
	}
	
	public void tryConnection () {
		if (origin != null && origin.vectO != this) {
			return;
		}
		
		for (int j = 0; j < 3; ++j) {
			if (origin == null || origin.vect [j] == null) {
				setVect (j);
			}
		}
	}
	
	void setVect (int n) {
		int markerId = mod_BuildCraftBuilders.markerBlock.blockID;
		
		int [] coords = new int [3];
		
		coords [0] = xCoord;
		coords [1] = yCoord;
		coords [2] = zCoord;
		
		if (origin == null || origin.vect [n] == null) {
			for (int j = 1; j < maxSize; ++j) {
				coords [n] += j;
								
				int blockId = worldObj.getBlockId(coords[0], coords[1],
						coords[2]);
				
				if (blockId == markerId) {
					TileMarker marker = (TileMarker) worldObj
							.getBlockTileEntity(coords[0], coords[1],
									coords[2]);
					
					if (marker.origin == null) {
						if (origin == null) {
							origin = new Origin();
							origin.vectO = this;
						}
						
						marker.origin = origin;
						origin.vect [n] = marker;
						
						createLasers ();
						
						break;
					}					
				}
				
				coords [n] -= j;
				coords [n] -= j;
				
				blockId = worldObj.getBlockId(coords[0], coords[1],
						coords[2]);
				
				if (blockId == markerId) {
					TileMarker marker = (TileMarker) worldObj
							.getBlockTileEntity(coords[0], coords[1],
									coords[2]);
					
					if (marker.origin == null) {
						if (origin == null) {
							origin = new Origin();
							origin.vectO = this;
						}
						
						marker.origin = origin;
						origin.vect [n] = marker;						
						
						createLasers ();
						
						break;
					}					
				}
				
				coords [n] += j;
			}
		}
	}
	
	class Coord {
		double x, y, z;
		
		public Coord (double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public boolean equals (Object o) {
			return ((Coord) o).x == x && ((Coord) o).y == y
					&& ((Coord) o).z == z;
		}
	}
	
	private void createLasers () {
		if (lasers != null) {
			for (EntityBlock entity : lasers) {
				if (entity != null) {
					entity.isDead = true;
				}
			}
		}
		
		lasers = new EntityBlock [12];
		
		int xMin, yMin, zMin;
		int xMax, yMax, zMax;		
		
		if (origin.vect [0] == null) {
			xMin = origin.vectO.xCoord;
			xMax = origin.vectO.xCoord;
		} else if (origin.vect [0].xCoord < xCoord){
			xMin = origin.vect [0].xCoord;
			xMax = xCoord;
		} else {
			xMin = xCoord;
			xMax = origin.vect [0].xCoord;
		}
		
		if (origin.vect [1] == null) {
			yMin = origin.vectO.yCoord;
			yMax = origin.vectO.yCoord;
		} else if (origin.vect [1].yCoord < yCoord){
			yMin = origin.vect [1].yCoord;
			yMax = yCoord;
		} else {
			yMin = yCoord;
			yMax = origin.vect [1].yCoord;
		}
		
		if (origin.vect [2] == null) {
			zMin = origin.vectO.zCoord;
			zMax = origin.vectO.zCoord;
		} else if (origin.vect [2].zCoord < zCoord){
			zMin = origin.vect [2].zCoord;
			zMax = zCoord;
		} else {
			zMin = zCoord;
			zMax = origin.vect [2].zCoord;
		}
		
		Coord [] p = new Coord [8];
		
		p [0] = new Coord(xMin, yMin, zMin);
		p [1] = new Coord(xMax, yMin, zMin);
		p [2] = new Coord(xMin, yMax, zMin);
		p [3] = new Coord(xMax, yMax, zMin);
		p [4] = new Coord(xMin, yMin, zMax);
		p [5] = new Coord(xMax, yMin, zMax);
		p [6] = new Coord(xMin, yMax, zMax);
		p [7] = new Coord(xMax, yMax, zMax);
		
		lasers [0] = createLaser (p [0], p [1]);
		lasers [1] = createLaser (p [0], p [2]);
		lasers [2] = createLaser (p [2], p [3]);
		lasers [3] = createLaser (p [1], p [3]);
		lasers [4] = createLaser (p [4], p [5]);
		lasers [5] = createLaser (p [4], p [6]);
		lasers [6] = createLaser (p [5], p [7]);
		lasers [7] = createLaser (p [6], p [7]);
		lasers [8] = createLaser (p [0], p [4]);
		lasers [9] = createLaser (p [1], p [5]);
		lasers [10] = createLaser (p [2], p [6]);
		lasers [11] = createLaser (p [3], p [7]);		
	}
	
	private EntityBlock createLaser (Coord p1, Coord p2) {
		if (p1.equals(p2)) {
			return null;
		}
		
		double iSize = p2.x - p1.x;
		double jSize = p2.y - p1.y;
		double kSize = p2.z - p1.z;
		
		double i = p1.x;
		double j = p1.y;
		double k = p1.z;
		
		if (iSize != 0) {
			System.out.println ("ISIZE = " + iSize);
			i += 0.5;
			j += 0.45;
			k += 0.45;
			
			jSize = 0.10;
			kSize = 0.10;
		} else if (jSize != 0) {			
			i += 0.45;
			j += 0.5;
			k += 0.45;
			
			iSize = 0.10;
			kSize = 0.10;
		} else if (kSize != 0) {
			i += 0.45;
			j += 0.45;
			k += 0.5;
			
			iSize = 0.10;
			jSize = 0.10;
		}
		
		EntityBlock block = new EntityBlock(worldObj, i, j, k, iSize, jSize,
				kSize, Block.bedrock.blockID,
				mod_BuildCraftBuilders.laserTexture);
		
		worldObj.entityJoinedWorld(block);
		
		return block;
	}
}
