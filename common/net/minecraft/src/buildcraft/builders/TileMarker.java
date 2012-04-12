/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.builders;

import net.minecraft.src.BuildCraftBuilders;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IAreaProvider;
import net.minecraft.src.buildcraft.api.LaserKind;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.TileBuildCraft;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.network.PacketTileUpdate;
import net.minecraft.src.buildcraft.core.network.PacketUpdate;

public class TileMarker extends TileBuildCraft implements IAreaProvider {
	
	private static int maxSize = 64;
	
	public static class TileWrapper {
		public @TileNetworkData int x, y, z;
		
		public TileWrapper () {
			x = Integer.MAX_VALUE;
			y = Integer.MAX_VALUE;
			z = Integer.MAX_VALUE;
		}
		
		public TileWrapper (int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		private TileMarker marker;
		
		public boolean isSet () {
			return x != Integer.MAX_VALUE;
		}
		
		public TileMarker getMarker (World world) {
			if (!isSet()) {
				return null;
			}
			
			if (marker == null) {
				marker = (TileMarker) world.getBlockTileEntity(x, y, z);
			}
			
			return marker;
		}
		
		public void reset () {
			x = Integer.MAX_VALUE;
			y = Integer.MAX_VALUE;
			z = Integer.MAX_VALUE;
		}
	}
	
	public static class Origin {		
		public boolean isSet () {
			return vectO.isSet();
		}
		
		public @TileNetworkData TileWrapper vectO = new TileWrapper();		
		public @TileNetworkData	(staticSize = 3) TileWrapper [] vect = {new TileWrapper (), new TileWrapper (), new TileWrapper ()};
		public @TileNetworkData	int xMin, yMin, zMin, xMax, yMax, zMax;		
	}
	
	public @TileNetworkData	Origin origin = new Origin();
	
	private EntityBlock [] lasers;		
	private EntityBlock [] signals;
	
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
			if (!origin.isSet() || !origin.vect [0].isSet()) {
				signals[0] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord + maxSize - 1,
						yCoord, zCoord), LaserKind.Blue);
				signals[1] = Utils.createLaser(worldObj, new Position(xCoord
						- maxSize + 1, yCoord, zCoord), new Position(xCoord,
						yCoord, zCoord), LaserKind.Blue);
			}
			
			if (!origin.isSet() || !origin.vect [1].isSet()) {
				signals[2] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord, zCoord), new Position(xCoord, yCoord + maxSize
						- 1, zCoord), LaserKind.Blue);
				signals[3] = Utils.createLaser(worldObj, new Position(xCoord,
						yCoord - maxSize + 1, zCoord), new Position(xCoord,
						yCoord, zCoord), LaserKind.Blue);
			}
			
			if (!origin.isSet() || !origin.vect [2].isSet()) {
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
		super.initialize();
		
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
		if (APIProxy.isClient(worldObj)) {
			return;
		}
		
		for (int j = 0; j < 3; ++j) {
			if (!origin.isSet() || !origin.vect [j].isSet()) {
				setVect (j);
			}
		}
		
		sendNetworkUpdate();
	}
	
	void setVect (int n) {
		int markerId = BuildCraftBuilders.markerBlock.blockID;
		
		int [] coords = new int [3];
		
		coords [0] = xCoord;
		coords [1] = yCoord;
		coords [2] = zCoord;
		
		if (!origin.isSet() || !origin.vect [n].isSet()) {
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
		
		if (origin.isSet() && marker.origin.isSet()) {
			return false;
		}
		
		if (!origin.isSet() && !marker.origin.isSet()) {
			origin = new Origin();
			marker.origin = origin;
			origin.vectO = new TileWrapper(xCoord, yCoord, zCoord);
			origin.vect[n] = new TileWrapper(marker.xCoord, marker.yCoord,
					marker.zCoord);
		} else if (!origin.isSet()) {
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
		
		if (!origin.vect [0].isSet()) {
			o.xMin = origin.vectO.x;
			o.xMax = origin.vectO.x;
		} else if (origin.vect [0].x < xCoord){
			o.xMin = origin.vect [0].x;
			o.xMax = xCoord;
		} else {
			o.xMin = xCoord;
			o.xMax = origin.vect [0].x;
		}
		
		if (!origin.vect [1].isSet()) {
			o.yMin = origin.vectO.y;
			o.yMax = origin.vectO.y;
		} else if (origin.vect [1].y < yCoord){
			o.yMin = origin.vect [1].y;
			o.yMax = yCoord;
		} else {
			o.yMin = yCoord;
			o.yMax = origin.vect [1].y;
		}
		
		if (!origin.vect [2].isSet()) {
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
		if (origin.isSet()) {
			return origin.xMin;
		}
		return xCoord;
	}

	@Override
	public int yMin() {
		if (origin.isSet()) {
			return origin.yMin;
		}
		return yCoord;
	}

	@Override
	public int zMin() {
		if (origin.isSet()) {
			return origin.zMin;
		}
		return zCoord;
	}

	@Override
	public int xMax() {
		if (origin.isSet()) {
			return origin.xMax;
		}
		return xCoord;
	}

	@Override
	public int yMax() {
		if (origin.isSet()) {
			return origin.yMax;
		}
		return yCoord;
	}

	@Override
	public int zMax() {
		if (origin.isSet()) {
			return origin.zMax;
		}
		return zCoord;
	}
	
	@Override
	public void invalidate () {
		destroy ();
	}
	
	@Override
	public void destroy () {
		TileMarker markerOrigin = null;
		
		if (origin.isSet()) {
			markerOrigin = origin.vectO.getMarker(worldObj);
			
			Origin o = origin;
			
			if (markerOrigin != null && markerOrigin.lasers != null) {
				for (EntityBlock e : markerOrigin.lasers) {
					if (e != null) {
						e.setDead();
					}
				}			
			}
						
			for (TileWrapper m : o.vect) {
				TileMarker mark = m.getMarker(worldObj);
				
				if (mark != null) {
					mark.lasers = null;
					
					if (mark != this) {
						mark.origin = new Origin();
					}
				}
			}
						
			markerOrigin.lasers = null;		
			
			if (markerOrigin != this) {
				markerOrigin.origin = new Origin();
			}
			
			for (TileWrapper m : o.vect) {
				TileMarker mark = m.getMarker(worldObj);
				
				if (mark != null) {
					mark.switchSignals();
				}
			}
			
			markerOrigin.switchSignals();						
		}
		
		if (signals != null) {
			for (EntityBlock b : signals) {
				if (b != null) {
					b.setDead();
				}
			}
		}				
		
		signals = null;
		
		if (APIProxy.isServerSide() && markerOrigin != null
				&& markerOrigin != this) {
			markerOrigin.sendNetworkUpdate();
		}
	}
	
	public void removeFromWorld () {
		if (!origin.isSet()) {
			return;
		}
		
		Origin o = origin;
		
		for (TileWrapper m : o.vect.clone()) {
			if (m.isSet()) {
				worldObj.setBlockWithNotify(m.x, m.y, m.z, 0);
				
				BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj,
						m.x, m.y, m.z,
						BuildCraftBuilders.markerBlock.blockID, 0);
			}
		}
		
		worldObj.setBlockWithNotify(o.vectO.x, o.vectO.y,
				o.vectO.z, 0);
		
		BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj,
				o.vectO.x, o.vectO.y, o.vectO.z,
				BuildCraftBuilders.markerBlock.blockID, 0);
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

		if (origin.isSet() && origin.vectO.getMarker(worldObj) == this) {
			NBTTagCompound vectO = new NBTTagCompound();

			new Position(origin.vectO.getMarker(worldObj)).writeToNBT(vectO);
			nbttagcompound.setTag("vectO", vectO);

			for (int i = 0; i < 3; ++i) {
				if (origin.vect[i].isSet()) {
					NBTTagCompound vect = new NBTTagCompound();
					new Position(origin.vect[i].x, origin.vect[i].y,
							origin.vect[i].z).writeToNBT(vect);
					nbttagcompound.setTag("vect" + i, vect);
				}
			}

		}
	}
	
	public Packet getDescriptionPacket() {		
		if (origin.vectO.getMarker(worldObj) == this) {
			return super.getDescriptionPacket();
		} else {
			return null;
		}
	}
	
	public Packet getUpdatePacket () {	
		TileMarker marker = origin.vectO.getMarker(worldObj);
			
		if (marker == this || marker == null) {
			return super.getUpdatePacket();
		} else if (marker != null) {
			marker.sendNetworkUpdate();			
		}
		
		return null;
	}

	@Override
	public void postPacketHandling (PacketUpdate packet) {
		super.postPacketHandling(packet);
		
		if (origin.vectO.isSet()) {		
			origin.vectO.getMarker(worldObj).switchSignals();
		
			for (TileWrapper w : origin.vect) {
				TileMarker m = w.getMarker(worldObj);
				
				if (m != null) {
					m.switchSignals();
				}
			}
		}
		
		createLasers();
	}
	
}
