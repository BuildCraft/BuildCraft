package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.PacketIds;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.TilePacketWrapper;
import net.minecraft.src.buildcraft.core.TileNetworkData;
import net.minecraft.src.buildcraft.core.Utils;

public class TileMarker extends TileBuildCraft implements IAreaProvider,
		ISynchronizedTile {
	
	private static TilePacketWrapper updatePacket = new TilePacketWrapper(
			TileMarker.class, PacketIds.TileUpdate);
	private static TilePacketWrapper desciptionPacket = new TilePacketWrapper(
			TileMarker.class, PacketIds.TileDescription);
	
	private static int maxSize = 64;
	
	private static class TileWrapper {
		@TileNetworkData
		public int x, y, z;
		
		public TileWrapper (int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		private TileMarker marker;
		
		public TileMarker getMarker (World world) {
			if (marker == null) {
				marker = (TileMarker) world.getBlockTileEntity(x, y, z);
			}
			
			return marker;
		}
	}
	
	private class Origin {
		@TileNetworkData
		TileWrapper vectO;
		
		@TileNetworkData
		TileWrapper [] vect = new TileWrapper [3];
	
		@TileNetworkData
		int xMin, yMin, zMin, xMax, yMax, zMax;		
	}
	
	@TileNetworkData
	Origin origin;
	
	EntityBlock [] lasers;
		
	EntityBlock signals [];
	
	public void switchSignals () {		
		if (signals != null) {
			for (EntityBlock b : signals) {
				if (b != null) {
					APIProxy.removeEntity(b);
				}
			}
			
			signals = null;
		}			
		
		if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {			
			signals = new EntityBlock [6];
			if (origin == null || origin.vect [0] == null) {
				signals[0] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord + maxSize - 1,
						yCoord, zCoord), LaserKind.Blue);
				signals[1] = Utils.createLaser(worldObj, new Position(xCoord
						- maxSize + 1, yCoord, zCoord), new Position(xCoord,
						yCoord, zCoord), LaserKind.Blue);
			}
			
			if (origin == null || origin.vect [1] == null) {
				signals[2] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord, yCoord + maxSize
						- 1, zCoord), LaserKind.Blue);
				signals[3] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord - maxSize + 1, zCoord), new Position(xCoord,
						yCoord, zCoord), LaserKind.Blue);
			}
			
			if (origin == null || origin.vect [2] == null) {
				signals[4] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord, yCoord, zCoord
						+ maxSize - 1), LaserKind.Blue);
				signals[5] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord - maxSize + 1), new Position(xCoord,
						yCoord, zCoord), LaserKind.Blue);
			}
		}
		
		if (APIProxy.isServerSide()) {
			sendNetworkUpdate();
		}
	}
	
	private Position initVectO, initVect []; 
	
	@Override
	public void initialize () {
		Utils.handleBufferedDescription(this);
		
		switchSignals ();
		
		if (initVectO != null) {
			origin = new Origin();
			
			origin.vectO = new TileWrapper((int) initVectO.x,
					(int) initVectO.y, (int) initVectO.z);
			
			for (int i = 0; i < 3; ++i) {
				if (initVect [i] != null) {
					linkTo((TileMarker) worldObj
					.getBlockTileEntity((int) initVect [i].x,
							(int) initVect [i].y, (int) initVect [i].z), i);
				}
			}
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
		int markerId = BuildCraftBuilders.markerBlock.blockID;
		
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
		if (marker == null) {
			return false;
		}
		
		if (origin != null && marker.origin != null) {
			return false;
		}
		
		if (origin == null && marker.origin == null) {
			origin = new Origin();
			marker.origin = origin;
			origin.vectO = new TileWrapper(xCoord, yCoord, zCoord);
			origin.vect[n] = new TileWrapper(marker.xCoord, marker.yCoord,
					marker.zCoord);
		} else if (origin == null) {
			origin = marker.origin;
			origin.vect [n] = new TileWrapper(xCoord, yCoord, zCoord);
		} else {						
			marker.origin = origin;
			origin.vect[n] = new TileWrapper(marker.xCoord, marker.yCoord,
					marker.zCoord);
		}
	
		origin.vectO.getMarker(worldObj).createLasers();
		switchSignals ();
		marker.switchSignals();

		return true;
	}
		
	private void createLasers () {
		if (lasers != null) {
			for (EntityBlock entity : lasers) {
				if (entity != null) {
					APIProxy.removeEntity(entity);
				}
			}
		}
		
		lasers = new EntityBlock [12];
		Origin o = origin;
		
		if (origin.vect [0] == null) {
			o.xMin = origin.vectO.x;
			o.xMax = origin.vectO.x;
		} else if (origin.vect [0].x < xCoord){
			o.xMin = origin.vect [0].x;
			o.xMax = xCoord;
		} else {
			o.xMin = xCoord;
			o.xMax = origin.vect [0].x;
		}
		
		if (origin.vect [1] == null) {
			o.yMin = origin.vectO.y;
			o.yMax = origin.vectO.y;
		} else if (origin.vect [1].y < yCoord){
			o.yMin = origin.vect [1].y;
			o.yMax = yCoord;
		} else {
			o.yMin = yCoord;
			o.yMax = origin.vect [1].y;
		}
		
		if (origin.vect [2] == null) {
			o.zMin = origin.vectO.z;
			o.zMax = origin.vectO.z;
		} else if (origin.vect [2].z < zCoord){
			o.zMin = origin.vect [2].z;
			o.zMax = zCoord;
		} else {
			o.zMin = zCoord;
			o.zMax = origin.vect [2].z;
		}
		
		lasers = Utils.createLaserBox(worldObj, o.xMin, o.yMin, o.zMin, o.xMax,
				o.yMax, o.zMax, LaserKind.Red);
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
	
	@Override
	public void destroy () {
		if (origin != null) {
			Origin o = origin;
			
			for (EntityBlock e : o.vectO.getMarker(worldObj).lasers) {
				if (e != null) {
					e.setEntityDead();
				}
			}			
						
			for (TileWrapper m : o.vect) {
				if (m != null) {
					m.getMarker(worldObj).lasers = null;
					m.getMarker(worldObj).origin = null;
				}
			}
						
			o.vectO.getMarker(worldObj).lasers = null;
			o.vectO.getMarker(worldObj).origin = null;
			
			for (TileWrapper m : o.vect) {
				if (m != null) {
					m.getMarker(worldObj).switchSignals();
				}
			}
			
			o.vectO.getMarker(worldObj).switchSignals();
		}
		
		if (signals != null) {
			for (EntityBlock b : signals) {
				b.setEntityDead();
			}
		}
		
		signals = null;
		origin = null;
	}
	
	public void removeFromWorld () {
		if (origin == null) {
			return;
		}
		
		Origin o = origin;
		
		for (TileWrapper m : o.vect) {
			if (m != null) {
				worldObj.setBlockWithNotify(m.x, m.y, m.z, 0);
				
				BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj,
						m.x, m.y, m.z,
						BuildCraftBuilders.markerBlock.blockID);
			}
		}
		
		worldObj.setBlockWithNotify(o.vectO.x, o.vectO.y,
				o.vectO.z, 0);
		
		BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj,
				o.vectO.x, o.vectO.y, o.vectO.z,
				BuildCraftBuilders.markerBlock.blockID);
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

		if (origin != null && origin.vectO.getMarker(worldObj) == this) {
			NBTTagCompound vectO = new NBTTagCompound();

			new Position(origin.vectO.getMarker(worldObj)).writeToNBT(vectO);
			nbttagcompound.setTag("vectO", vectO);

			for (int i = 0; i < 3; ++i) {
				if (origin.vect[i] != null) {
					NBTTagCompound vect = new NBTTagCompound();
					new Position(origin.vect[i].getMarker(worldObj))
							.writeToNBT(vect);
					nbttagcompound.setTag("vect" + i, vect);
				}
			}

		}
	}
	
	public Packet getDescriptionPacket() {		
		return desciptionPacket.toPacket(this);
	}
	
	public Packet230ModLoader getUpdatePacket () {				
		return updatePacket.toPacket(this);
	}
	
	@Override
	public void handleDescriptionPacket (Packet230ModLoader packet) {		
		desciptionPacket.updateFromPacket(this, packet);
		
		switchSignals();		
		createLasers();
	}

	@Override
	public void handleUpdatePacket(Packet230ModLoader packet) {
		updatePacket.updateFromPacket(this, packet);
				
		switchSignals();
		createLasers();
	}
	
}
