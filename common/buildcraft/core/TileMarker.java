/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;
import buildcraft.api.tiles.ITileAreaProvider;
import buildcraft.core.lib.EntityBlock;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.utils.LaserUtils;
import buildcraft.core.proxy.CoreProxy;

public class TileMarker extends TileBuildCraft implements ITileAreaProvider {
	public static class TileWrapper implements ISerializable {

		public int x, y, z;
		private TileMarker marker;

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

		public boolean isSet() {
			return x != Integer.MAX_VALUE;
		}

		public TileMarker getMarker(World world) {
			if (!isSet()) {
				return null;
			}

			if (marker == null) {
				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile instanceof TileMarker) {
					marker = (TileMarker) tile;
				}
			}

			return marker;
		}

		public void reset() {
			x = Integer.MAX_VALUE;
			y = Integer.MAX_VALUE;
			z = Integer.MAX_VALUE;
		}

		@Override
		public void readData(ByteBuf stream) {
			x = stream.readInt();
			if (isSet()) {
				y = stream.readShort();
				z = stream.readInt();
			}
		}

		@Override
		public void writeData(ByteBuf stream) {
			stream.writeInt(x);
			if (isSet()) {
				// Only X is used for checking if a vector is set, so we can save space on the Y coordinate.
				stream.writeShort(y);
				stream.writeInt(z);
			}
		}
	}

	public static class Origin implements ISerializable {
		public TileWrapper vectO = new TileWrapper();
		public TileWrapper[] vect = {new TileWrapper(), new TileWrapper(), new TileWrapper()};
		public int xMin, yMin, zMin, xMax, yMax, zMax;

		public boolean isSet() {
			return vectO.isSet();
		}

		@Override
		public void writeData(ByteBuf stream) {
			vectO.writeData(stream);
			for (TileWrapper tw : vect) {
				tw.writeData(stream);
			}
			stream.writeInt(xMin);
			stream.writeShort(yMin);
			stream.writeInt(zMin);
			stream.writeInt(xMax);
			stream.writeShort(yMax);
			stream.writeInt(zMax);
		}

		@Override
		public void readData(ByteBuf stream) {
			vectO.readData(stream);
			for (TileWrapper tw : vect) {
				tw.readData(stream);
			}
			xMin = stream.readInt();
			yMin = stream.readShort();
			zMin = stream.readInt();
			xMax = stream.readInt();
			yMax = stream.readShort();
			zMax = stream.readInt();
		}
	}

	public Origin origin = new Origin();
	public boolean showSignals = false;

	private Position initVectO;
	private Position[] initVect;
	private EntityBlock[] lasers;
	private EntityBlock[] signals;

	public void updateSignals() {
		if (!worldObj.isRemote) {
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
				signals[0] = LaserUtils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord + DefaultProps.MARKER_RANGE - 1, yCoord, zCoord),
						LaserKind.Blue);
				signals[1] = LaserUtils.createLaser(worldObj, new Position(xCoord - DefaultProps.MARKER_RANGE + 1, yCoord, zCoord), new Position(xCoord, yCoord, zCoord),
						LaserKind.Blue);
			}

			if (!origin.isSet() || !origin.vect[1].isSet()) {
				signals[2] = LaserUtils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord, yCoord + DefaultProps.MARKER_RANGE - 1, zCoord),
						LaserKind.Blue);
				signals[3] = LaserUtils.createLaser(worldObj, new Position(xCoord, yCoord - DefaultProps.MARKER_RANGE + 1, zCoord), new Position(xCoord, yCoord, zCoord),
						LaserKind.Blue);
			}

			if (!origin.isSet() || !origin.vect[2].isSet()) {
				signals[4] = LaserUtils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord), new Position(xCoord, yCoord, zCoord + DefaultProps.MARKER_RANGE - 1),
						LaserKind.Blue);
				signals[5] = LaserUtils.createLaser(worldObj, new Position(xCoord, yCoord, zCoord - DefaultProps.MARKER_RANGE + 1), new Position(xCoord, yCoord, zCoord),
						LaserKind.Blue);
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		updateSignals();

		if (initVectO != null) {
			origin = new Origin();

			origin.vectO = new TileWrapper((int) initVectO.x, (int) initVectO.y, (int) initVectO.z);

			for (int i = 0; i < 3; ++i) {
				if (initVect[i] != null) {
					linkTo((TileMarker) worldObj.getTileEntity((int) initVect[i].x, (int) initVect[i].y, (int) initVect[i].z), i);
				}
			}
		}
	}

	public void tryConnection() {
		if (worldObj.isRemote) {
			return;
		}

		for (int j = 0; j < 3; ++j) {
			if (!origin.isSet() || !origin.vect[j].isSet()) {
				setVect(j);
			}
		}

		sendNetworkUpdate();
	}

	void setVect(int n) {
		int[] coords = new int[3];

		coords[0] = xCoord;
		coords[1] = yCoord;
		coords[2] = zCoord;

		if (!origin.isSet() || !origin.vect[n].isSet()) {
			for (int j = 1; j < DefaultProps.MARKER_RANGE; ++j) {
				coords[n] += j;

				Block block = worldObj.getBlock(coords[0], coords[1], coords[2]);

				if (block == BuildCraftCore.markerBlock) {
					TileMarker marker = (TileMarker) worldObj.getTileEntity(coords[0], coords[1], coords[2]);

					if (linkTo(marker, n)) {
						break;
					}
				}

				coords[n] -= j;
				coords[n] -= j;

				block = worldObj.getBlock(coords[0], coords[1], coords[2]);

				if (block == BuildCraftCore.markerBlock) {
					TileMarker marker = (TileMarker) worldObj.getTileEntity(coords[0], coords[1], coords[2]);

					if (linkTo(marker, n)) {
						break;
					}
				}

				coords[n] += j;
			}
		}
	}

	private boolean linkTo(TileMarker marker, int n) {
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

		lasers = LaserUtils.createLaserBox(worldObj, o.xMin, o.yMin, o.zMin, o.xMax, o.yMax, o.zMax, LaserKind.Red);
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

		if (!worldObj.isRemote && markerOrigin != null && markerOrigin != this) {
			markerOrigin.sendNetworkUpdate();
		}
	}

	@Override
	public void removeFromWorld() {
		if (!origin.isSet()) {
			return;
		}

		Origin o = origin;

		for (TileWrapper m : o.vect.clone()) {
			if (m.isSet()) {
				worldObj.setBlockToAir(m.x, m.y, m.z);

				BuildCraftCore.markerBlock.dropBlockAsItem(worldObj, m.x, m.y, m.z, 0, 0);
			}
		}

		worldObj.setBlockToAir(o.vectO.x, o.vectO.y, o.vectO.z);

		BuildCraftCore.markerBlock.dropBlockAsItem(worldObj, o.vectO.x, o.vectO.y, o.vectO.z, 0, 0);
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
	public void writeData(ByteBuf stream) {
		origin.writeData(stream);
		stream.writeBoolean(showSignals);
	}

	@Override
	public void readData(ByteBuf stream) {
		origin.readData(stream);
		showSignals = stream.readBoolean();

		switchSignals();

		if (origin.vectO.isSet() && origin.vectO.getMarker(worldObj) != null) {
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

	@Override
	public boolean isValidFromLocation(int x, int y, int z) {
		// Rules:
		// - one or two, but not three, of the coordinates must be equal to the marker's location
		// - one of the coordinates must be either -1 or 1 away
		// - it must be physically touching the box
		// - however, it cannot be INSIDE the box
		int equal = (x == xCoord ? 1 : 0) + (y == yCoord ? 1 : 0) + (z == zCoord ? 1 : 0);
		int touching = 0;

		if (equal == 0 || equal == 3) {
			return false;
		}

		if (x < (xMin() - 1) || x > (xMax() + 1) || y < (yMin() - 1) || y > (yMax() + 1)
				|| z < (zMin() - 1) || z > (zMax() + 1)) {
			return false;
		}

		if (x >= xMin() && x <= xMax() && y >= yMin() && y <= yMax() && z >= zMin() && z <= zMax()) {
			return false;
		}

		if (xMin() - x == 1 || x - xMax() == 1) {
			touching++;
		}

		if (yMin() - y == 1 || y - yMax() == 1) {
			touching++;
		}

		if (zMin() - z == 1 || z - zMax() == 1) {
			touching++;
		}

		return touching == 1;
	}
}
