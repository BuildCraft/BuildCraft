package net.minecraft.src.buildcraft.builders;

import java.util.LinkedList;
import java.util.TreeSet;

import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.EntityLaser;
import net.minecraft.src.buildcraft.core.WorldIterator;
import net.minecraft.src.buildcraft.core.WorldIteratorRadius;

public class TilePathMarker extends TileMarker {

	public EntityLaser lasers[] = new EntityLaser[2];
	
	public int x0, y0, z0, x1, y1, z1;
	public boolean loadLink0 = false, loadLink1 = false;
	
	public TilePathMarker links[] = new TilePathMarker[2];

	public static int searchSize = 64;

	public boolean isFullyConnected() {
		return lasers[0] != null && lasers[1] != null;
	}

	public boolean isLinkedTo(TilePathMarker pathMarker) {
		return links[0] == pathMarker || links[1] == pathMarker;
	}

	public void connect(TilePathMarker marker, EntityLaser laser) {
		if (lasers[0] == null) {
			lasers[0] = laser;
			links[0] = marker;
		} else if (lasers[1] == null) {
			lasers[1] = laser;
			links[1] = marker;
		}
	}
	
	public void createLaserAndConnect (TilePathMarker pathMarker) {
		EntityLaser laser = new EntityLaser(worldObj);
		laser.setPositions(xCoord + 0.5, yCoord + 0.5,
				zCoord + 0.5, pathMarker.xCoord + 0.5,
				pathMarker.yCoord + 0.5, pathMarker.zCoord + 0.5);
		laser.setTexture("/net/minecraft/src/buildcraft/core/gui/laser_1.png");
		worldObj.spawnEntityInWorld(laser);

		connect(pathMarker, laser);
		pathMarker.connect(this, laser);
	}

	WorldIterator currentWorldIterator;

	@Override
	public void tryConnection() {
		if (isFullyConnected()) {
			return;
		}

		if (currentWorldIterator == null) {
			currentWorldIterator = new WorldIteratorRadius(worldObj, xCoord,
					yCoord, zCoord, searchSize);
			worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if (currentWorldIterator != null) {
			for (int i = 0; i < 1000; ++i) {
				BlockIndex b = currentWorldIterator.iterate();

				if (b == null) {
					currentWorldIterator = null;
					worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
					break;
				}

				if (b.i == xCoord && b.j == yCoord && b.k == zCoord) {
					continue;
				}

				TileEntity tile = null;
				
				try {
					tile = worldObj.getBlockTileEntity(b.i, b.j, b.k);
				} catch (Throwable t) {
					// sometimes, tile can't be loaded. Just carry on the
					// analysis. We don't even need to log these
				}

				if (tile instanceof TilePathMarker) {
					TilePathMarker pathMarker = (TilePathMarker) tile;

					if (!pathMarker.isFullyConnected()
							&& !isLinkedTo(pathMarker)) {
						createLaserAndConnect(pathMarker);

						worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
						currentWorldIterator = null;
						return;
					}
				}
			}
		}
	}
	
	public LinkedList <BlockIndex> getPath () {
		TreeSet<BlockIndex> visitedPaths = new TreeSet <BlockIndex> ();
		LinkedList <BlockIndex> res = new LinkedList <BlockIndex> ();
		
		TilePathMarker nextTile = this;
		
		while (nextTile != null) {
			BlockIndex b = new BlockIndex (nextTile.xCoord, nextTile.yCoord, nextTile.zCoord);
			
			visitedPaths.add(b);
			res.add(b);			
			
			if (nextTile.links [0] != null && !visitedPaths.contains(new BlockIndex(nextTile.links [0].xCoord, nextTile.links [0].yCoord, nextTile.links [0].zCoord))) {
				nextTile = nextTile.links [0];
			} else if (nextTile.links [1] != null && !visitedPaths.contains(new BlockIndex(nextTile.links [1].xCoord, nextTile.links [1].yCoord, nextTile.links [1].zCoord))) {
				nextTile = nextTile.links [1];
			} else {
				nextTile = null;
			}
		}
		
		return res;		
		
	}

	@Override
	public void invalidate () {
		super.invalidate();
		
		if (lasers [0] != null) {
			links [0].unlink (this);
			lasers [0].setDead();
		}
		
		if (lasers [1] != null) {
			links [1].unlink (this);
			lasers [1].setDead();
		}
		
		lasers = new EntityLaser [2];
		links = new TilePathMarker [2];
	}
	
	@Override
	public void initialize () {
		super.initialize();

		if (loadLink0) {
			TileEntity e0 = worldObj.getBlockTileEntity(x0, y0, z0);

			if (links[0] != e0 && links[1] != e0
					&& e0 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e0);
			}

			loadLink0 = false;
		}

		if (loadLink1) {
			TileEntity e1 = worldObj.getBlockTileEntity(x1, y1, z1);

			if (links[0] != e1 && links[1] != e1
					&& e1 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e1);
			}

			loadLink1 = false;
		}
	}

	private void unlink(TilePathMarker tile) {
		if (links [0] == tile) {
			lasers [0] = null;
			links [0] = null;
		}
		
		if (links [1] == tile) {
			lasers [1] = null;
			links [1] = null;
		}				
	}
	
	@Override
	 public void readFromNBT(NBTTagCompound nbttagcompound) {
		 super.readFromNBT(nbttagcompound);
		 
		 if (nbttagcompound.hasKey("x0")) {
			 x0 = nbttagcompound.getInteger("x0");
			 y0 = nbttagcompound.getInteger("y0");
			 z0 = nbttagcompound.getInteger("z0");
			 
			 loadLink0 = true;
		 }
		 
		 if (nbttagcompound.hasKey("x1")) {
			 x1 = nbttagcompound.getInteger("x1");
			 y1 = nbttagcompound.getInteger("y1");
			 z1 = nbttagcompound.getInteger("z1");
			 
			 loadLink1 = true;
		 }
	 }

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		 if (links [0] != null) {
			 nbttagcompound.setInteger("x0", links [0].xCoord);
			 nbttagcompound.setInteger("y0", links [0].yCoord);
			 nbttagcompound.setInteger("z0", links [0].zCoord);
		 }
		 
		 if (links [1] != null) {
			 nbttagcompound.setInteger("x0", links [1].xCoord);
			 nbttagcompound.setInteger("y0", links [1].yCoord);
			 nbttagcompound.setInteger("z0", links [1].zCoord);
		 }
	}
}
