package buildcraft.builders;

import buildcraft.api.core.Position;
import buildcraft.core.BlockIndex;
import buildcraft.core.EntityLaser;
import buildcraft.core.EntityPowerLaser;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TilePathMarker extends TileMarker {

	public EntityLaser lasers[] = new EntityLaser[2];
	public int x0, y0, z0, x1, y1, z1;
	public boolean loadLink0 = false, loadLink1 = false;
	public @TileNetworkData
	boolean tryingToConnect = false;
	public TilePathMarker links[] = new TilePathMarker[2];
	public static int searchSize = 64; // TODO: this should be moved to default props
	// A list with the pathMarkers that aren't fully connected
	// It only contains markers within the loaded chunks
	private static LinkedList<TilePathMarker> availableMarkers = new LinkedList<TilePathMarker>();

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

		if (isFullyConnected()) {
			availableMarkers.remove(this);
		}
	}

	public void createLaserAndConnect(TilePathMarker pathMarker) {

		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		EntityPowerLaser laser = new EntityPowerLaser(worldObj, new Position(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5), new Position(pathMarker.xCoord + 0.5,
				pathMarker.yCoord + 0.5, pathMarker.zCoord + 0.5));
		laser.show();

		laser.setTexture(0);
		worldObj.spawnEntityInWorld(laser);

		connect(pathMarker, laser);
		pathMarker.connect(this, laser);
	}

	// Searches the availableMarkers list for the nearest available that is within searchSize
	private TilePathMarker findNearestAvailablePathMarker() {
		TilePathMarker nearestAvailable = null;
		double nearestDistance = 0, distance; // The initialization of nearestDistance is only to make the compiler shut up

		for (TilePathMarker t : availableMarkers) {
			if (t == this || t == this.links[0] || t == this.links[1] || t.worldObj.provider.dimensionId != this.worldObj.provider.dimensionId) {
				continue;
			}

			distance = Math.sqrt(Math.pow(this.xCoord - t.xCoord, 2) + Math.pow(this.yCoord - t.yCoord, 2) + Math.pow(this.zCoord - t.zCoord, 2));

			if (distance > searchSize) {
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

		if (CoreProxy.proxy.isRenderWorld(worldObj) || isFullyConnected())
			return;

		tryingToConnect = !tryingToConnect; // Allow the user to stop the path marker from searching for new path markers to connect
		sendNetworkUpdate();
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;

		if (tryingToConnect) {
			TilePathMarker nearestPathMarker = findNearestAvailablePathMarker();

			if (nearestPathMarker != null) {
				createLaserAndConnect(nearestPathMarker);
				tryingToConnect = false;
				sendNetworkUpdate();
			}
		}
	}

	public LinkedList<BlockIndex> getPath() {
		TreeSet<BlockIndex> visitedPaths = new TreeSet<BlockIndex>();
		LinkedList<BlockIndex> res = new LinkedList<BlockIndex>();

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

		if (lasers[0] != null) {
			links[0].unlink(this);
			lasers[0].setDead();
		}

		if (lasers[1] != null) {
			links[1].unlink(this);
			lasers[1].setDead();
		}

		availableMarkers.remove(this);
		tryingToConnect = false;
	}

	@Override
	public void initialize() {
		super.initialize();

		if (CoreProxy.proxy.isSimulating(worldObj) && !isFullyConnected()) {
			availableMarkers.add(this);
		}

		if (loadLink0) {
			TileEntity e0 = worldObj.getBlockTileEntity(x0, y0, z0);

			if (links[0] != e0 && links[1] != e0 && e0 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e0);
			}

			loadLink0 = false;
		}

		if (loadLink1) {
			TileEntity e1 = worldObj.getBlockTileEntity(x1, y1, z1);

			if (links[0] != e1 && links[1] != e1 && e1 instanceof TilePathMarker) {
				createLaserAndConnect((TilePathMarker) e1);
			}

			loadLink1 = false;
		}
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

		if (!isFullyConnected() && !availableMarkers.contains(this) && CoreProxy.proxy.isSimulating(worldObj)) {
			availableMarkers.add(this);
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
		availableMarkers.remove(this);
	}

	public static void clearAvailableMarkersList() {
		availableMarkers.clear();
	}

	public static void clearAvailableMarkersList(World w) {
		for (Iterator<TilePathMarker> it = availableMarkers.iterator(); it.hasNext();) {
			TilePathMarker t = it.next();
			if (t.worldObj.provider.dimensionId != w.provider.dimensionId) {
				it.remove();
			}
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		boolean previousState = tryingToConnect;

		super.handleUpdatePacket(packet);

		if (previousState != tryingToConnect) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
}
