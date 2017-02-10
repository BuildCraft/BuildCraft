package buildcraft.robotics.map;

import java.io.File;
import java.util.Date;

import com.google.common.collect.HashBiMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

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

    private static boolean doUpdate(MapWorld world, Chunk chunk) {
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
        World world = event.getWorld();
        MapWorld map = worldMap.get(world);
        if (map != null) {
            map.save();
            synchronized (worldMap) {
                worldMap.remove(world);
            }
        }
    }

    @SubscribeEvent
    public void chunkLoaded(ChunkEvent.Load event) {
        updateChunkDelayed(event.getWorld(), event.getChunk(), false, (byte) (40 + Utils.RANDOM.nextInt(20)));
    }

    @SubscribeEvent
    public void chunkUnloaded(ChunkEvent.Unload event) {
        updateChunk(event.getWorld(), event.getChunk(), false);
    }

    @SubscribeEvent
    public void blockPlaced(BlockEvent.PlaceEvent placeEvent) {
        World world = placeEvent.getWorld();
        BlockPos pos = placeEvent.getPos();
        Chunk chunk = world.getChunkFromBlockCoords(pos);
        MapWorld map = getWorld(world);
        if (map != null && doUpdate(map, chunk)) {
            int hv = world.getHeight(pos).getY();
            if (pos.getY() >= (hv - 3)) {
                map.updateChunk(chunk);
            }
        }
    }

    @SubscribeEvent
    public void blockBroken(BlockEvent.BreakEvent placeEvent) {
        Chunk chunk = placeEvent.world.getChunkFromBlockCoords(placeEvent.pos);
        MapWorld world = getWorld(placeEvent.world);
        if (world != null && doUpdate(world, chunk)) {
            int hv = placeEvent.world.getHeight(placeEvent.pos).getY();
            if (placeEvent.pos.getY() >= (hv - 3)) {
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
            } catch (InterruptedException ie) {
               ie.printStackTrace();
            }
        }
    }

    public void initialize() {
        for (WorldServer ws : DimensionManager.getWorlds()) {
            MapWorld mw = getWorld(ws);
            IChunkProvider provider = ws.getChunkProvider();
            if (provider instanceof ChunkProviderServer) {
                for (Chunk c : ((ChunkProviderServer) provider).getLoadedChunks()) {
                    if (c != null && !mw.hasChunk(c.xPosition, c.zPosition)) {
                            mw.updateChunkDelayed(c, (byte) (40 + Utils.RANDOM.nextInt(20)));
                        }
                }
            }
        }
    }
}
