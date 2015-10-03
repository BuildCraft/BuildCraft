package buildcraft.robotics.map;

import java.io.File;
import java.util.Date;

import com.google.common.collect.HashBiMap;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import buildcraft.core.lib.utils.Utils;

public class MapManager implements Runnable {
	private static final int UPDATE_DELAY = 60000;
	private final HashBiMap<World, MapWorld> worldMap = HashBiMap.create();
	private final File location;
	private boolean stop = false;
	private long lastSaveTime;

	public MapManager(File location) {
		this.location = location;
	}

	public void stop() {
		stop = true;
		saveAllWorlds();
	}

	public MapWorld getWorld(World world) {
		if (world == null || world.isRemote) {
			return null;
		}

		if (!worldMap.containsKey(world)) {
			synchronized (worldMap) {
				worldMap.put(world, new MapWorld(world, location));
			}
		}
		return worldMap.get(world);
	}

	private boolean doUpdate(MapWorld world, Chunk chunk) {
		int x = chunk.xPosition;
		int z = chunk.zPosition;
		long updateTime = (new Date()).getTime() - UPDATE_DELAY;
		return world.getUpdateTime(x, z) < updateTime || !world.hasChunk(x, z);
	}

	private void updateChunk(World rworld, Chunk chunk, boolean force) {
		MapWorld world = getWorld(rworld);
		if (world != null && (force || doUpdate(world, chunk))) {
			world.updateChunk(chunk);
		}
	}

	private void updateChunkDelayed(World rworld, Chunk chunk, boolean force, byte time) {
		MapWorld world = getWorld(rworld);
		if (world != null && (force || doUpdate(world, chunk))) {
			world.updateChunkDelayed(chunk, time);
		}
	}

	@SubscribeEvent
	public void tickDelayedWorlds(TickEvent.WorldTickEvent event) {
		if (event.phase == TickEvent.Phase.END && event.side == Side.SERVER) {
			MapWorld w = worldMap.get(event.world);
			if (w != null) {
				w.tick();
			}
		}
	}

	@SubscribeEvent
	public void worldUnloaded(WorldEvent.Unload event) {
		if (worldMap.containsKey(event.world)) {
			worldMap.get(event.world).save();
			synchronized (worldMap) {
				worldMap.remove(event.world);
			}
		}
	}

	@SubscribeEvent
	public void chunkLoaded(ChunkEvent.Load event) {
		updateChunkDelayed(event.world, event.getChunk(), false, (byte) (40 + Utils.RANDOM.nextInt(20)));
	}

	@SubscribeEvent
	public void chunkUnloaded(ChunkEvent.Unload event) {
		updateChunk(event.world, event.getChunk(), false);
	}

	@SubscribeEvent
	public void blockPlaced(BlockEvent.PlaceEvent placeEvent) {
		Chunk chunk = placeEvent.world.getChunkFromBlockCoords(placeEvent.x, placeEvent.z);
		MapWorld world = getWorld(placeEvent.world);
		if (world != null && doUpdate(world, chunk)) {
			int hv = placeEvent.world.getHeightValue(placeEvent.x, placeEvent.z);
			if (placeEvent.y >= (hv - 3)) {
				world.updateChunk(chunk);
			}
		}
	}

	@SubscribeEvent
	public void blockBroken(BlockEvent.BreakEvent placeEvent) {
		Chunk chunk = placeEvent.world.getChunkFromBlockCoords(placeEvent.x, placeEvent.z);
		MapWorld world = getWorld(placeEvent.world);
		if (world != null && doUpdate(world, chunk)) {
			int hv = placeEvent.world.getHeightValue(placeEvent.x, placeEvent.z);
			if (placeEvent.y >= (hv - 3)) {
				world.updateChunk(chunk);
			}
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
			long now = (new Date()).getTime();

			if (now - lastSaveTime > 120000) {
				saveAllWorlds();
				lastSaveTime = now;
			}

			try {
				Thread.sleep(4000);
			} catch (Exception e) {

			}
		}
	}

	public void initialize() {
		for (WorldServer ws : DimensionManager.getWorlds()) {
			MapWorld mw = getWorld(ws);
			IChunkProvider provider = ws.getChunkProvider();
			if (provider instanceof ChunkProviderServer) {
				for (Object o : ((ChunkProviderServer) provider).func_152380_a()) {
					if (o != null && o instanceof Chunk) {
						Chunk c = (Chunk) o;
						if (!mw.hasChunk(c.xPosition, c.zPosition)) {
							mw.updateChunkDelayed(c, (byte) (40 + Utils.RANDOM.nextInt(20)));
						}
					}
				}
			}
		}
	}
}
