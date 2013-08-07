/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Position;
import buildcraft.core.EntityBlock;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TileMarker extends TileBuildCraft implements IAreaProvider {

	private static int maxSize = 64;

	public static class TileWrapper {

		public @TileNetworkData
		int x, y, z;

		public TileWrapper() {
			x = Integer.MAX_VALUE;
			y = Integer.MAX_VALUE;
			z = Integer.MAX_VALUE;
		}

		public TileWrapper(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		private TileMarker marker;

		public boolean isSet() {
			return x != Integer.MAX_VALUE;
		}

		public TileMarker getMarker(World world) {
			if (!isSet())
				return null;

			if (marker == null) {
				marker = (TileMarker) world.getBlockTileEntity(x, y, z);
			}

			return marker;
		}

		public void reset() {
			x = Integer.MAX_VALUE;
			y = Integer.MAX_VALUE;
			z = Integer.MAX_VALUE;
		}
	}

	public static class Origin {

		public boolean isSet() {
			return vectO.isSet();
		}

		public @TileNetworkData
		TileWrapper vectO = new TileWrapper();
		public @TileNetworkData(staticSize = 3)
		TileWrapper[] vect = { new TileWrapper(), new TileWrapper(), new TileWrapper() };
		public @TileNetworkData
		int xMin, yMin, zMin, xMax, yMax, zMax;
	}

	public @TileNetworkData
	Origin origin = new Origin();

	private EntityBlock[] lasers;
	private EntityBlock[] signals;
	public @TileNetworkData
	boolean showSignals = false;

	public void updateSignals() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			showSignals = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
			sendNetworkUpdate();
		}
	}

	private void switchSignals() {
		if (signals != null) {
			for (EntityBlock b : signals) {
				if (b != null) {
					CoreProxy.proxy.removeEntity(b);
				}
			}
			signals = null;
		}
		if (showSignals) {
			signals = new EntityBlock[6];
			if (!origin.isSet() || !origin.vect[0].isSet()) {
				signals[0] = Utils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord + maxSize - 1, yCoord, zCoord),
						LaserKind.Blue);
				signals[1] = Utils.createLaser(worldObj, new Position(xCoord - maxSize + 1, yCoord, zCoord), new Position(xCoord, yCoord, zCoord),
						LaserKind.Blue);
			}

			if (!origin.isSet() || !origin.vect[1].isSet()) {
				signals[2] = Utils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord, yCoord + maxSize - 1, zCoord),
						LaserKind.Blue);
				signals[3] = Utils.createLaser(worldObj, new Position(xCoord, yCoord - maxSize + 1, zCoord), new Position(xCoord, yCoord, zCoord),
						LaserKind.Blue);
			}

			if (!origin.isSet() || !origin.vect[2].isSet()) {
				signals[4] = Utils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord, yCoord, zCoord + maxSize - 1),
						LaserKind.Blue);
				signals[5] = Utils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord - maxSize + 1), new Position(xCoord, yCoord, zCoord),
						LaserKind.Blue);
			}
		}
	}

	private Position initVectO, initVect[];

	@Override
	public void initialize() {
		super.initialize();

		updateSignals();

		if (initVectO != null) {
			origin = new Origin();

			origin.vectO = new TileWrapper((int) initVectO.x, (int) initVectO.y, (int) initVectO.z);

			for (int i = 0; i < 3; ++i) {
				if (initVect[i] != null) {
					linkTo((TileMarker) worldObj.getBlockTileEntity((int) initVect[i].x, (int) initVect[i].y, (int) initVect[i].z), i);
				}
			}
		}
	}

	public void tryConnection() {
		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		for (int j = 0; j < 3; ++j) {
			if (!origin.isSet() || !origin.vect[j].isSet()) {
				setVect(j);
			}
		}

		sendNetworkUpdate();
	}

	void setVect(int n) {
		int markerId = BuildCraftBuilders.markerBlock.blockID;

		int[] coords = new int[3];

		coords[0] = xCoord;
		coords[1] = yCoord;
		coords[2] = zCoord;

		if (!origin.isSet() || !origin.vect[n].isSet()) {
			for (int j = 1; j < maxSize; ++j) {
				coords[n] += j;

				int blockId = worldObj.getBlockId(coords[0], coords[1], coords[2]);

				if (blockId == markerId) {
					TileMarker marker = (TileMarker) worldObj.getBlockTileEntity(coords[0], coords[1], coords[2]);

					if (linkTo(marker, n)) {
						break;
					}
				}

				coords[n] -= j;
				coords[n] -= j;

				blockId = worldObj.getBlockId(coords[0], coords[1], coords[2]);

				if (blockId == markerId) {
					TileMarker marker = (TileMarker) worldObj.getBlockTileEntity(coords[0], coords[1], coords[2]);

					if (linkTo(marker, n)) {
						break;
					}
				}

				coords[n] += j;
			}
		}
	}

	private boolean linkTo(TileMarker marker, int n) {
		if (marker == null)
			return false;

		if (origin.isSet() && marker.origin.isSet())
			return false;

		if (!origin.isSet() && !marker.origin.isSet()) {
			origin = new Origin();
			marker.origin = origin;
			origin.vectO = new TileWrapper(xCoord, yCoord, zCoord);
			origin.vect[n] = new TileWrapper(marker.xCoord, marker.yCoord, marker.zCoord);
		} else if (!origin.isSet()) {
			origin = marker.origin;
			origin.vect[n] = new TileWrapper(xCoord, yCoord, zCoord);
		} else {
			marker.origin = origin;
			origin.vect[n] = new TileWrapper(marker.xCoord, marker.yCoord, marker.zCoord);
		}

		origin.vectO.getMarker(worldObj).createLasers();
		updateSignals();
		marker.updateSignals();

		return true;
	}

	private void createLasers() {
		if (lasers != null) {
			for (EntityBlock entity : lasers) {
				if (entity != null) {
					CoreProxy.proxy.removeEntity(entity);
				}
			}
		}

		lasers = new EntityBlock[12];
		Origin o = origin;

		if (!origin.vect[0].isSet()) {
			o.xMin = origin.vectO.x;
			o.xMax = origin.vectO.x;
		} else if (origin.vect[0].x < xCoord) {
			o.xMin = origin.vect[0].x;
			o.xMax = xCoord;
		} else {
			o.xMin = xCoord;
			o.xMax = origin.vect[0].x;
		}

		if (!origin.vect[1].isSet()) {
			o.yMin = origin.vectO.y;
			o.yMax = origin.vectO.y;
		} else if (origin.vect[1].y < yCoord) {
			o.yMin = origin.vect[1].y;
			o.yMax = yCoord;
		} else {
			o.yMin = yCoord;
			o.yMax = origin.vect[1].y;
		}

		if (!origin.vect[2].isSet()) {
			o.zMin = origin.vectO.z;
			o.zMax = origin.vectO.z;
		} else if (origin.vect[2].z < zCoord) {
			o.zMin = origin.vect[2].z;
			o.zMax = zCoord;
		} else {
			o.zMin = zCoord;
			o.zMax = origin.vect[2].z;
		}

		lasers = Utils.createLaserBox(worldObj, o.xMin, o.yMin, o.zMin, o.xMax, o.yMax, o.zMax, LaserKind.Red);
	}

	@Override
	public int xMin() {
		if (origin.isSet())
			return origin.xMin;
		return xCoord;
	}

	@Override
	public int yMin() {
		if (origin.isSet())
			return origin.yMin;
		return yCoord;
	}

	@Override
	public int zMin() {
		if (origin.isSet())
			return origin.zMin;
		return zCoord;
	}

	@Override
	public int xMax() {
		if (origin.isSet())
			return origin.xMax;
		return xCoord;
	}

	@Override
	public int yMax() {
		if (origin.isSet())
			return origin.yMax;
		return yCoord;
	}

	@Override
	public int zMax() {
		if (origin.isSet())
			return origin.zMax;
		return zCoord;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		TileMarker markerOrigin = null;

		if (origin.isSet()) {
			markerOrigin = origin.vectO.getMarker(worldObj);

			Origin o = origin;

			if (markerOrigin != null && markerOrigin.lasers != null) {
				for (EntityBlock entity : markerOrigin.lasers) {
					if (entity != null) {
						entity.setDead();
					}
				}
				markerOrigin.lasers = null;
			}

			for (TileWrapper m : o.vect) {
				TileMarker mark = m.getMarker(worldObj);

				if (mark != null) {
					if (mark.lasers != null) {
						for (EntityBlock entity : mark.lasers) {
							if (entity != null) {
								entity.setDead();
							}
						}
						mark.lasers = null;
					}

					if (mark != this) {
						mark.origin = new Origin();
					}
				}
			}

			if (markerOrigin != this && markerOrigin != null) {
				markerOrigin.origin = new Origin();
			}

			for (TileWrapper wrapper : o.vect) {
				TileMarker mark = wrapper.getMarker(worldObj);

				if (mark != null) {
					mark.updateSignals();
				}
			}
			if (markerOrigin != null) {
				markerOrigin.updateSignals();
			}
		}

		if (signals != null) {
			for (EntityBlock block : signals) {
				if (block != null) {
					block.setDead();
				}
			}
		}

		signals = null;

		if (CoreProxy.proxy.isSimulating(worldObj) && markerOrigin != null && markerOrigin != this) {
			markerOrigin.sendNetworkUpdate();
		}
	}

	@Override
	public void removeFromWorld() {
		if (!origin.isSet())
			return;

		Origin o = origin;

		for (TileWrapper m : o.vect.clone()) {
			if (m.isSet()) {
				worldObj.setBlock(m.x, m.y, m.z, 0);

				BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj, m.x, m.y, m.z, BuildCraftBuilders.markerBlock.blockID, 0);
			}
		}

		worldObj.setBlock(o.vectO.x, o.vectO.y, o.vectO.z, 0);

		BuildCraftBuilders.markerBlock.dropBlockAsItem(worldObj, o.vectO.x, o.vectO.y, o.vectO.z, BuildCraftBuilders.markerBlock.blockID, 0);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		if (nbttagcompound.hasKey("vectO")) {
			initVectO = new Position(nbttagcompound.getCompoundTag("vectO"));
			initVect = new Position[3];

			for (int i = 0; i < 3; ++i) {
				if (nbttagcompound.hasKey("vect" + i)) {
					initVect[i] = new Position(nbttagcompound.getCompoundTag("vect" + i));
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (origin.isSet() && origin.vectO.getMarker(worldObj) == this) {
			NBTTagCompound vectO = new NBTTagCompound();

			new Position(origin.vectO.getMarker(worldObj)).writeToNBT(vectO);
			nbttagcompound.setTag("vectO", vectO);

			for (int i = 0; i < 3; ++i) {
				if (origin.vect[i].isSet()) {
					NBTTagCompound vect = new NBTTagCompound();
					new Position(origin.vect[i].x, origin.vect[i].y, origin.vect[i].z).writeToNBT(vect);
					nbttagcompound.setTag("vect" + i, vect);
				}
			}

		}
	}

	@Override
	public void postPacketHandling(PacketUpdate packet) {
		super.postPacketHandling(packet);

		switchSignals();

		if (origin.vectO.isSet()) {
			origin.vectO.getMarker(worldObj).updateSignals();

			for (TileWrapper w : origin.vect) {
				TileMarker m = w.getMarker(worldObj);

				if (m != null) {
					m.updateSignals();
				}
			}
		}

		createLasers();
	}

}
