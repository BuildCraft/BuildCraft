/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.Position;

public class TilePathMarker extends TileMarker implements IPathProvider {
	// A list with the pathMarkers that aren't fully connected
	// It only contains markers within the loaded chunks
	private static ArrayList<TilePathMarker> availableMarkers = new ArrayList<TilePathMarker>();

	public int x0, y0, z0, x1, y1, z1;
	public boolean loadLink0 = false;
	public boolean loadLink1 = false;

	public LaserData[] lasers = new LaserData[2];
	public boolean tryingToConnect = false;

	public TilePathMarker[] links = new TilePathMarker[2];

	public boolean isFullyConnected() {
		return lasers[0] != null && lasers[1] != null;
	}

	public boolean isLinkedTo(TilePathMarker pathMarker) {
		return links[0] == pathMarker || links[1] == pathMarker;
	}

	public void connect(TilePathMarker marker, LaserData laser) {
		if (lasers[0] == null) {
			lasers[0] = laser;
			links[0] = marker;
		} else if (lasers[1] == null) {
			lasers[1] = laser;
			links[1] = marker;
		}

		if (isFullyConnected()) {
			availableMarkers.remove(this);
		}
	}

	public void createLaserAndConnect(TilePathMarker pathMarker) {
		if (worldObj.isRemote) {
			return;
		}

		LaserData laser = new LaserData
				(new Position(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5),
						new Position(pathMarker.xCoord + 0.5, pathMarker.yCoord + 0.5, pathMarker.zCoord + 0.5));

		LaserData laser2 = new LaserData(laser.head, laser.tail);
		laser2.isVisible = false;

		connect(pathMarker, laser);
		pathMarker.connect(this, laser2);
	}

	/**
	 * Searches the availableMarkers list for the nearest available that is
	 * within searchSize
	 */
	private TilePathMarker findNearestAvailablePathMarker() {
		TilePathMarker nearestAvailable = null;
		double nearestDistance = 0, distance;

		for (TilePathMarker t : availableMarkers) {
			if (t == this || t == this.links[0] || t == this.links[1] || t.getWorldObj().provider.dimensionId != this.getWorldObj().provider.dimensionId) {
				continue;
			}

			int dx = this.xCoord - t.xCoord;
			int dy = this.yCoord - t.yCoord;
			int dz = this.zCoord - t.zCoord;
			distance = dx * dx + dy * dy + dz * dz;

			if (distance > DefaultProps.MARKER_RANGE * DefaultProps.MARKER_RANGE) {
				continue;
			}

			if (nearestAvailable == null || distance < nearestDistance) {
				nearestAvailable = t;
				nearestDistance = distance;
			}
		}

		return nearestAvailable;
	}

	@Override
	public void tryConnection() {

		if (worldObj.isRemote || isFullyConnected()) {
			return;
		}

		// Allow the user to stop the path marker from searching for new path markers to connect
		tryingToConnect = !tryingToConnect;

		sendNetworkUpdate();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (tryingToConnect) {
			TilePathMarker nearestPathMarker = findNearestAvailablePathMarker();

			if (nearestPathMarker != null) {
				createLaserAndConnect(nearestPathMarker);
			}

			tryingToConnect = false;

			sendNetworkUpdate();
			getWorldObj().markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord,
					xCoord, yCoord, zCoord);
		}
	}

	@Override
	public ArrayList<BlockIndex> getPath() {
		HashSet<BlockIndex> visitedPaths = new HashSet<BlockIndex>();
		ArrayList<BlockIndex> res = new ArrayList<BlockIndex>();

		TilePathMarker nextTile = this;

		while (nextTile != null) {
			BlockIndex b = new BlockIndex(nextTile.xCoord, nextTile.yCoord, nextTile.zCoord);

			visitedPaths.add(b);
			res.add(b);

			if (nextTile.links[0] != null
					&& !visitedPaths.contains(new BlockIndex(nextTile.links[0].xCoord, nextTile.links[0].yCoord, nextTile.links[0].zCoord))) {
				nextTile = nextTile.links[0];
			} else if (nextTile.links[1] != null
					&& !visitedPaths.contains(new BlockIndex(nextTile.links[1].xCoord, nextTile.links[1].yCoord, nextTile.links[1].zCoord))) {
				nextTile = nextTile.links[1];
			} else {
				nextTile = null;
			}
		}

		return res;

	}

	@Override
	public void invalidate() {
		super.invalidate();

		if (links[0] != null) {
			links[0].unlink(this);
		}

		if (links[1] != null) {
			links[1].unlink(this);
		}

		availableMarkers.remove(this);
		tryingToConnect = false;
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!worldObj.isRemote && !isFullyConnected()) {
			availableMarkers.add(this);
		}

		if (loadLink0) {
			TileEntity e0 = worldObj.getTileEntity(x0, y0, z0);

			if (links[0] != e0 && links[1] != e0 && e0 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e0);
			}

			loadLink0 = false;
		}

		if (loadLink1) {
			TileEntity e1 = worldObj.getTileEntity(x1, y1, z1);

			if (links[0] != e1 && links[1] != e1 && e1 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e1);
			}

			loadLink1 = false;
		}

		sendNetworkUpdate();
	}

	private void unlink(TilePathMarker tile) {
		if (links[0] == tile) {
			lasers[0] = null;
			links[0] = null;
		}

		if (links[1] == tile) {
			lasers[1] = null;
			links[1] = null;
		}

		if (!isFullyConnected() && !availableMarkers.contains(this) && !worldObj.isRemote) {
			availableMarkers.add(this);
		}

		sendNetworkUpdate();
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

		if (links[0] != null) {
			nbttagcompound.setInteger("x0", links[0].xCoord);
			nbttagcompound.setInteger("y0", links[0].yCoord);
			nbttagcompound.setInteger("z0", links[0].zCoord);
		}

		if (links[1] != null) {
			nbttagcompound.setInteger("x1", links[1].xCoord);
			nbttagcompound.setInteger("y1", links[1].yCoord);
			nbttagcompound.setInteger("z1", links[1].zCoord);
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		availableMarkers.remove(this);
	}

	public static void clearAvailableMarkersList() {
		availableMarkers.clear();
	}

	public static void clearAvailableMarkersList(World w) {
		for (Iterator<TilePathMarker> it = availableMarkers.iterator(); it.hasNext();) {
			TilePathMarker t = it.next();
			if (t.getWorldObj().provider.dimensionId == w.provider.dimensionId) {
				it.remove();
			}
		}
	}

	@Override
	public void readData(ByteBuf data) {
		boolean previousState = tryingToConnect;

		int flags = data.readUnsignedByte();
		if ((flags & 1) != 0) {
			lasers[0] = new LaserData();
			lasers[0].readData(data);
		} else {
			lasers[0] = null;
		}
		if ((flags & 2) != 0) {
			lasers[1] = new LaserData();
			lasers[1].readData(data);
		} else {
			lasers[1] = null;
		}
		tryingToConnect = (flags & 4) != 0;

		if (previousState != tryingToConnect) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		int flags = (lasers[0] != null ? 1 : 0) | (lasers[1] != null ? 2 : 0) | (tryingToConnect ? 4 : 0);
		data.writeByte(flags);
		if (lasers[0] != null) {
			lasers[0].writeData(data);
		}
		if (lasers[1] != null) {
			lasers[1].writeData(data);
		}
	}

	@Override
	public void removeFromWorld() {
		List<BlockIndex> path = getPath();
		for (BlockIndex b : path) {
			BuildCraftCore.pathMarkerBlock.dropBlockAsItem(
					worldObj, b.x, b.y, b.z,
					0, 0);

			worldObj.setBlockToAir(b.x, b.y, b.z);
		}
	}
}
