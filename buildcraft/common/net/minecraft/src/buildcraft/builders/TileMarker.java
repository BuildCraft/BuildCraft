package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftBuilders;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.Position;
import net.minecraft.src.buildcraft.core.Utils;

public class TileMarker extends TileEntity implements IAreaProvider {
	
	private static int maxSize = 64;
	
	private class Origin {
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
				signals[0] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord + maxSize - 1,
						yCoord, zCoord), 1);
				signals[1] = Utils.createLaser(worldObj, new Position(xCoord
						- maxSize + 1, yCoord, zCoord), new Position(xCoord,
						yCoord, zCoord), 1);
			}
			
			if (origin == null || origin.vect [1] == null) {
				signals[2] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord, yCoord + maxSize
						- 1, zCoord), 1);
				signals[3] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord - maxSize + 1, zCoord), new Position(xCoord,
						yCoord, zCoord), 1);
			}
			
			if (origin == null || origin.vect [2] == null) {
				signals[4] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord, yCoord, zCoord
						+ maxSize - 1), 1);
				signals[5] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord - maxSize + 1), new Position(xCoord,
						yCoord, zCoord), 1);
			}
		}
	}
	
	boolean init = false;
	
	private Position initVectO, initVect []; 
	
	public void updateEntity() {
		if (!init) {
			switchSignals ();
			
			if (initVectO != null) {
				origin = new Origin();
				
				origin.vectO = (TileMarker) worldObj
						.getBlockTileEntity((int) initVectO.i,
								(int) initVectO.j, (int) initVectO.k);
				
				for (int i = 0; i < 3; ++i) {
					if (initVect [i] != null) {
						linkTo((TileMarker) worldObj
						.getBlockTileEntity((int) initVect [i].i,
								(int) initVect [i].j, (int) initVect [i].k), i);
					}
				}
			}
			
			init = true;
		}
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
		
		lasers = Utils.createLaserBox(worldObj, o.xMin, o.yMin, o.zMin, o.xMax,
				o.yMax, o.zMax, 0);
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
		
		if (signals != null) {
			for (EntityBlock b : signals) {
				b.setEntityDead();
			}
		}
		
		signals = null;
	}
	
	public void removeFromWorld () {
		if (origin == null) {
			return;
		}
		
		Origin o = origin;
		
		for (TileMarker m : o.vect) {
			if (m != null) {
				worldObj.setBlockWithNotify(m.xCoord, m.yCoord, m.zCoord, 0);
				
				mod_BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj,
						m.xCoord, m.yCoord, m.zCoord,
						mod_BuildCraftBuilders.markerBlock.blockID);
			}
		}
		
		worldObj.setBlockWithNotify(o.vectO.xCoord, o.vectO.yCoord,
				o.vectO.zCoord, 0);
		
		mod_BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj,
				o.vectO.xCoord, o.vectO.yCoord, o.vectO.zCoord,
				mod_BuildCraftBuilders.markerBlock.blockID);
	}
	
	 public void readFromNBT(NBTTagCompound nbttagcompound) {
		 super.readFromNBT(nbttagcompound);
		 		 
		 if (nbttagcompound.hasKey("vectO")) {
			 initVectO = new Position(nbttagcompound.getCompoundTag("vectO"));
			 initVect = new Position [3];
			 
			 for (int i = 0; i < 3; ++i) {
				 if (nbttagcompound.hasKey("vect" + i)) {
					initVect[i] = new Position(
							nbttagcompound.getCompoundTag("vect" + i));
				 }
			 }			  
		 }
	 }

	 public void writeToNBT(NBTTagCompound nbttagcompound) {
		 super.writeToNBT(nbttagcompound);
		 
		 if (origin != null && origin.vectO == this) {
			 NBTTagCompound vectO = new NBTTagCompound();
			 
			 new Position (origin.vectO).writeToNBT(vectO);
			 nbttagcompound.setTag("vectO", vectO);
			 
			 for (int i = 0; i < 3; ++i) {
				 if (origin.vect [i] != null) {
					 NBTTagCompound vect = new NBTTagCompound();
					 new Position (origin.vect [i]).writeToNBT(vect);
					 nbttagcompound.setTag("vect" + i, vect);
				 }
			 }			 
			 
		 }
	 }
}
