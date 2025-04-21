package buildcraft.robotics.map;

import buildcraft.lib.misc.data.XorShift128Random;
import com.google.common.collect.HashBiMap;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.io.File;
import java.util.Date;

public class MapManager implements Runnable {
    private static final int UPDATE_DELAY = 60000;
    private final HashBiMap<World, MapWorld> worldMap = HashBiMap.create();
    private final File location;
    private boolean stop = false;
    private long lastSaveTime;
    public static final XorShift128Random rand = new XorShift128Random();

    public MapManager(File location) {
        this.location = location;
    }

    public void stop() {
        stop = true;
        saveAllWorlds();
    }

    public MapWorld getWorld(IWorld levelAccessor) {
        if (!(levelAccessor instanceof World)) {
            return null;
        }
        World world = (World) levelAccessor;
        if (world == null || world.isClientSide) {
            return null;
        }

        if (!worldMap.containsKey(world)) {
            synchronized (worldMap) {
                worldMap.put(world, new MapWorld(world, location));
            }
        }
        return worldMap.get(world);
    }

    private static boolean doUpdate(MapWorld world, IChunk chunk) {
        int x = chunk.getPos().x;
        int z = chunk.getPos().z;
        long updateTime = (new Date()).getTime() - UPDATE_DELAY;
        return world.getUpdateTime(x, z) < updateTime || !world.hasChunk(x, z);
    }

    private void updateChunk(IWorld rworld, IChunk chunk, boolean force) {
        MapWorld world = getWorld(rworld);
        // if (world != null && (force || doUpdate(world, chunk)))
        if (world != null && (chunk instanceof Chunk) && (force || doUpdate(world, chunk))) {
            world.updateChunk((Chunk) chunk);
        }
    }

    private void updateChunkDelayed(IWorld rworld, IChunk chunk, boolean force, byte time) {
        MapWorld world = getWorld(rworld);
        if (world != null && (force || doUpdate(world, chunk))) {
            world.updateChunkDelayed(chunk, time);
        }
    }

    @SubscribeEvent
    public void tickDelayedWorlds(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.SERVER) {
            MapWorld w = worldMap.get(event.world);
            if (w != null) {
                w.tick();
            }
        }
    }

    @SubscribeEvent
    public void worldUnloaded(WorldEvent.Unload event) {
        if (worldMap.containsKey(event.getWorld())) {
            worldMap.get(event.getWorld()).save();
            synchronized (worldMap) {
                worldMap.remove(event.getWorld());
            }
        }
    }

    @SubscribeEvent
    public void chunkLoaded(ChunkEvent.Load event) {
        // updateChunkDelayed(event.getWorld(), event.getChunk(), false, (byte) (40 + VecUtil.RANDOM.nextInt(20)));
        updateChunkDelayed(event.getWorld(), event.getChunk(), false, (byte) (40 + rand.nextInt(20)));
    }

    @SubscribeEvent
    public void chunkUnloaded(ChunkEvent.Unload event) {
        updateChunk(event.getWorld(), event.getChunk(), false);
    }

    @SubscribeEvent
    // public void blockPlaced(BlockEvent.PlaceEvent placeEvent)
    public void blockPlaced(BlockEvent.EntityPlaceEvent placeEvent) {
        // LevelChunk chunk = placeEvent.world.getChunkFromBlockCoords(placeEvent.pos);
        IChunk chunk = placeEvent.getWorld().getChunk(placeEvent.getPos());
        // MapWorld world = getWorld(placeEvent.world);
        MapWorld world = getWorld(placeEvent.getWorld());
        // if (world != null && doUpdate(world, chunk))
        if (world != null && chunk instanceof Chunk && doUpdate(world, chunk)) {
            // int hv = placeEvent.world.getHeight(placeEvent.pos).getY();
            int hv = placeEvent.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, placeEvent.getPos().getX(), placeEvent.getPos().getZ());
            // if (placeEvent.pos.getY() >= (hv - 3))
            if (placeEvent.getPos().getY() >= (hv - 3)) {
                world.updateChunk((Chunk) chunk);
            }
        }
    }

    @SubscribeEvent
    public void blockBroken(BlockEvent.BreakEvent placeEvent) {
        // Chunk chunk = placeEvent.world.getChunkFromBlockCoords(placeEvent.pos);
        IChunk chunk = placeEvent.getWorld().getChunk(placeEvent.getPos());
        MapWorld world = getWorld(placeEvent.getWorld());
        // if (world != null && doUpdate(world, chunk))
        if (world != null && chunk instanceof Chunk && doUpdate(world, chunk)) {
            // int hv = placeEvent.getWorld().getHeight(placeEvent.getPos()).getY();
            int hv = placeEvent.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, placeEvent.getPos().getX(), placeEvent.getPos().getZ());
            if (placeEvent.getPos().getY() >= (hv - 3)) {
                world.updateChunk((Chunk) chunk);
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

    // public void initialize()
    public void initialize(FMLServerStartingEvent event) {
        // for (WorldServer ws : DimensionManager.getWorlds())
        for (ServerWorld ws : event.getServer().getAllLevels()) {
            MapWorld mw = getWorld(ws);
            // IChunkProvider provider = ws.getChunkProvider();
            ServerChunkProvider provider = ws.getChunkSource();
            // if (provider instanceof ChunkProviderServer)
            if (provider instanceof ServerChunkProvider) {
                // for (Object o : ((ChunkProviderServer) provider).func_152380_a())
                for (ChunkHolder o : provider.chunkMap.visibleChunkMap.values()) {
                    // if (o != null && o instanceof Chunk)
                    if (o != null) {
                        // Chunk c = (Chunk) o;
                        IChunk c = o.getLastAvailable();
                        // if (!mw.hasChunk(c.xPosition, c.zPosition))
                        if (!mw.hasChunk(c.getPos().x, c.getPos().z)) {
                            // mw.updateChunkDelayed(c, (byte) (40 + VecUtil.RANDOM.nextInt(20)));
                            mw.updateChunkDelayed(c, (byte) (40 + rand.nextInt(20)));
                        }
                    }
                }
            }
        }
    }
}
