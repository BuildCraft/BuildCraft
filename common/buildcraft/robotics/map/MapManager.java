package buildcraft.robotics.map;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashBiMap;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;

public class MapManager implements Runnable {
	private final HashBiMap<World, MapWorld> worldMap = HashBiMap.create();
	private final Set<MapChunkLoadRequest> chunkLoadRequests = new HashSet<MapChunkLoadRequest>();
	private final File location;
	private boolean stop = false;

	private long lastSaveTime;

	public MapManager(File location) {
		this.location = location;
	}

	public void stop() {
		stop = true;
	}

	public MapWorld getWorld(World world) {
		if (world.isRemote) {
			return null;
		}

		if (!worldMap.containsKey(world)) {
			synchronized (worldMap) {
				worldMap.put(world, new MapWorld(world, location));
			}
		}
		return worldMap.get(world);
	}

	@SubscribeEvent
	public void chunkLoaded(ChunkEvent.Load event) {
		MapWorld world = getWorld(event.getChunk().worldObj);
		if (world != null) {
			world.queueChunkForUpdateIfEmpty(event.getChunk().xPosition, event.getChunk().zPosition, 99999);
		}
	}

	@SubscribeEvent
	public void blockPlaced(BlockEvent.PlaceEvent placeEvent) {
		Chunk chunk = placeEvent.world.getChunkFromBlockCoords(placeEvent.x, placeEvent.z);
		MapWorld world = getWorld(placeEvent.world);
		if (world != null) {
			world.queueChunkForUpdate(chunk.xPosition, chunk.zPosition, 512);
		}
	}

	@SubscribeEvent
	public void blockBroken(BlockEvent.BreakEvent placeEvent) {
		Chunk chunk = placeEvent.world.getChunkFromBlockCoords(placeEvent.x, placeEvent.z);
		MapWorld world = getWorld(placeEvent.world);
		if (world != null) {
			world.queueChunkForUpdate(chunk.xPosition, chunk.zPosition, 512);
		}
	}


	public void saveAllWorlds() {
		synchronized (worldMap) {
			for (MapWorld world : worldMap.values()) {
				world.save();
			}
		}
	}

	@Override
	public void run() {
		lastSaveTime = (new Date()).getTime();

		while (!stop) {
			synchronized (worldMap) {
				for (MapWorld world : worldMap.values()) {
					world.updateChunkInQueue();
				}
			}

			long now = (new Date()).getTime();

			if (now - lastSaveTime > 120000) {
				saveAllWorlds();
				lastSaveTime = now;
			}

			try {
				Thread.sleep(50 * worldMap.size());
			} catch (Exception e) {

			}
		}
	}

	@SubscribeEvent
	public void serverTickEnd(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			synchronized (chunkLoadRequests) {
				for (MapChunkLoadRequest r : chunkLoadRequests) {
					r.world.updateChunk(r.x, r.z);
				}
				chunkLoadRequests.clear();
			}
		}
	}

	public void loadChunkForUpdate(MapWorld mapWorld, int x, int z) {
		synchronized (chunkLoadRequests) {
			chunkLoadRequests.add(new MapChunkLoadRequest(mapWorld, x, z));
		}
	}
}
