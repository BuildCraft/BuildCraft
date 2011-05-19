package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.Block;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.IAreaProvider;

public class TileMarker extends TileEntity implements IAreaProvider {
	
	private static int maxSize = 64;
	
	public class Origin {
		TileMarker vectO;
		TileMarker [] vect = new TileMarker [3];
		
		int xMin, yMin, zMin;
		int xMax, yMax, zMax;		
	}
	
	Origin origin;
	
	EntityBlock [] lasers;
		
	EntityBlock signals [];
	
	public void switchSignals () {
		if (signals != null) {
			for (EntityBlock b : signals) {
				if (b != null) {
					b.setEntityDead();
				}
			}
			
			signals = null;
		}
		
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			signals = new EntityBlock [6];
			if (origin == null || origin.vect [0] == null) {
				signals [0] = createLaser(new Coord(xCoord, yCoord, zCoord), new Coord(xCoord + maxSize - 1, yCoord, zCoord), 1);
				signals [1] = createLaser(new Coord(xCoord - maxSize + 1, yCoord, zCoord), new Coord(xCoord, yCoord, zCoord), 1);
			}
			
			if (origin == null || origin.vect [1] == null) {
				signals [2] = createLaser(new Coord(xCoord, yCoord, zCoord), new Coord(xCoord, yCoord + maxSize - 1, zCoord), 1);
				signals [3] = createLaser(new Coord(xCoord, yCoord - maxSize + 1, zCoord), new Coord(xCoord, yCoord, zCoord), 1);
			}
			
			if (origin == null || origin.vect [2] == null) {
				signals [4] = createLaser(new Coord(xCoord, yCoord, zCoord), new Coord(xCoord, yCoord, zCoord + maxSize - 1), 1);
				signals [5] = createLaser(new Coord(xCoord, yCoord, zCoord - maxSize + 1), new Coord(xCoord, yCoord, zCoord), 1);
			}
		}
	}
	
	public void updateEntity() {
		
	}
	
	public void tryConnection () {		
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
					
					if (linkTo (marker, n)) {	
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
					
					if (linkTo (marker, n)) {	
						break;
					}
				}
				
				coords [n] += j;
			}
		}
	}
	
	private boolean linkTo (TileMarker marker, int n) {
		if (origin != null && marker.origin != null) {
			return false;
		}
		
		if (origin == null && marker.origin == null) {
			origin = new Origin();
			marker.origin = origin;
			origin.vectO = this;
			origin.vect [n] = marker;
		} else if (origin == null) {
			origin = marker.origin;
			origin.vect [n] = this;
		} else {						
			marker.origin = origin;
			origin.vect [n] = marker;
		}
	
		origin.vectO.createLasers ();
		switchSignals ();
		marker.switchSignals();

		return true;
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
		Origin o = origin;
		
		if (origin.vect [0] == null) {
			o.xMin = origin.vectO.xCoord;
			o.xMax = origin.vectO.xCoord;
		} else if (origin.vect [0].xCoord < xCoord){
			o.xMin = origin.vect [0].xCoord;
			o.xMax = xCoord;
		} else {
			o.xMin = xCoord;
			o.xMax = origin.vect [0].xCoord;
		}
		
		if (origin.vect [1] == null) {
			o.yMin = origin.vectO.yCoord;
			o.yMax = origin.vectO.yCoord;
		} else if (origin.vect [1].yCoord < yCoord){
			o.yMin = origin.vect [1].yCoord;
			o.yMax = yCoord;
		} else {
			o.yMin = yCoord;
			o.yMax = origin.vect [1].yCoord;
		}
		
		if (origin.vect [2] == null) {
			o.zMin = origin.vectO.zCoord;
			o.zMax = origin.vectO.zCoord;
		} else if (origin.vect [2].zCoord < zCoord){
			o.zMin = origin.vect [2].zCoord;
			o.zMax = zCoord;
		} else {
			o.zMin = zCoord;
			o.zMax = origin.vect [2].zCoord;
		}
		
		Coord [] p = new Coord [8];
		
		p [0] = new Coord(o.xMin, o.yMin, o.zMin);
		p [1] = new Coord(o.xMax, o.yMin, o.zMin);
		p [2] = new Coord(o.xMin, o.yMax, o.zMin);
		p [3] = new Coord(o.xMax, o.yMax, o.zMin);
		p [4] = new Coord(o.xMin, o.yMin, o.zMax);
		p [5] = new Coord(o.xMax, o.yMin, o.zMax);
		p [6] = new Coord(o.xMin, o.yMax, o.zMax);
		p [7] = new Coord(o.xMax, o.yMax, o.zMax);
		
		lasers [0] = createLaser (p [0], p [1], 0);
		lasers [1] = createLaser (p [0], p [2], 0);
		lasers [2] = createLaser (p [2], p [3], 0);
		lasers [3] = createLaser (p [1], p [3], 0);
		lasers [4] = createLaser (p [4], p [5], 0);
		lasers [5] = createLaser (p [4], p [6], 0);
		lasers [6] = createLaser (p [5], p [7], 0);
		lasers [7] = createLaser (p [6], p [7], 0);
		lasers [8] = createLaser (p [0], p [4], 0);
		lasers [9] = createLaser (p [1], p [5], 0);
		lasers [10] = createLaser (p [2], p [6], 0);
		lasers [11] = createLaser (p [3], p [7], 0);		
	}
	
	private EntityBlock createLaser (Coord p1, Coord p2, int color) {
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
		
		int texture;
		
		if (color == 0) {
			texture = mod_BuildCraftBuilders.redLaserTexture;
		} else {
			texture = mod_BuildCraftBuilders.blueLaserTexture;
		}
		
		EntityBlock block = new EntityBlock(worldObj, i, j, k, iSize, jSize,
				kSize, Block.bedrock.blockID,
				texture);
		
		worldObj.entityJoinedWorld(block);
		
		return block;
	}

	@Override
	public int xMin() {
		if (origin != null) {
			return origin.xMin;
		}
		return xCoord;
	}

	@Override
	public int yMin() {
		if (origin != null) {
			return origin.yMin;
		}
		return yCoord;
	}

	@Override
	public int zMin() {
		if (origin != null) {
			return origin.zMin;
		}
		return zCoord;
	}

	@Override
	public int xMax() {
		if (origin != null) {
			return origin.xMax;
		}
		return xCoord;
	}

	@Override
	public int yMax() {
		if (origin != null) {
			return origin.yMax;
		}
		return yCoord;
	}

	@Override
	public int zMax() {
		if (origin != null) {
			return origin.zMax;
		}
		return zCoord;
	}
	
	public void destroy () {
		if (origin != null) {
			Origin o = origin;
			
			for (EntityBlock e : o.vectO.lasers) {
				if (e != null) {
					e.setEntityDead();
				}
			}			
						
			for (TileMarker m : o.vect) {
				if (m != null) {
					m.lasers = null;
					m.origin = null;
				}
			}
						
			o.vectO.lasers = null;
			o.vectO.origin = null;
			
			for (TileMarker m : o.vect) {
				if (m != null) {
					m.switchSignals();
				}
			}
			
			o.vectO.switchSignals();
		}
	}
}
