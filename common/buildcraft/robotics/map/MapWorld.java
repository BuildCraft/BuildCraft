package buildcraft.robotics.map;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MapWorld {
    // private final LongHashMap regionMap;
    private final Long2ObjectOpenHashMap regionMap;
    // private final HashMap<Chunk, Integer> timeToUpdate = new HashMap<Chunk, Integer>();
    private final HashMap<ChunkAccess, Integer> timeToUpdate = new HashMap<ChunkAccess, Integer>();
    private final Long2LongOpenHashMap regionUpdateTime;
    private final LongOpenHashSet updatedChunks;
    private final File location;

    public MapWorld(Level world, File location) {
        // regionMap = new LongHashMap();
        regionMap = new Long2ObjectOpenHashMap();
        regionUpdateTime = new Long2LongOpenHashMap();
        updatedChunks = new LongOpenHashSet();

        // String saveFolder = world.provider.getSaveFolder();
        // if (saveFolder == null) {
        //     saveFolder = "world";
        // }
        // this.location = new File(location, saveFolder);
        this.location = DimensionType.getStorageFolder(world.dimension(), Paths.get(location.toURI())).toFile();
        try {
            this.location.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MapRegion getRegion(int x, int z) {
        long id = MapUtils.getIDFromCoords(x, z);
        // MapRegion region = (MapRegion) regionMap.getValueByKey(id);
        MapRegion region = (MapRegion) regionMap.get(id);
        if (region == null) {
            region = new MapRegion(x, z);

            // Check in the location first
            File target = new File(location, "r" + x + "," + z + ".nbt");
            if (target.exists()) {
                try {
                    // FileInputStream f = new FileInputStream(target);
                    // byte[] data = new byte[(int) target.length()];
                    // f.read(data);
                    // f.close();

                    // CompoundTag nbt = NBTUtilBC.load(data);
                    CompoundTag nbt = NbtIo.read(target);
                    if (nbt != null) {
                        region.readFromNBT(nbt);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // regionMap.add(id, region);
            regionMap.put(id, region);
        }
        return region;
    }

    private MapChunk getChunk(int x, int z) {
        MapRegion region = getRegion(x >> 4, z >> 4);
        return region.getChunk(x & 15, z & 15);
    }

    public boolean hasChunk(int x, int z) {
        MapRegion region = getRegion(x >> 4, z >> 4);
        return region.hasChunk(x & 15, z & 15);
    }

    public void save() {
        long[] chunkList;
        synchronized (updatedChunks) {
            chunkList = updatedChunks.toLongArray();
            updatedChunks.clear();
        }

        for (long id : chunkList) {
            // MapRegion region = (MapRegion) regionMap.getValueByKey(id);
            MapRegion region = (MapRegion) regionMap.get(id);
            if (region == null) {
                continue;
            }

            CompoundTag output = new CompoundTag();
            region.writeToNBT(output);
            // byte[] data = NBTUtilBC.save(output);
            File file = new File(location, "r" + MapUtils.getXFromID(id) + "," + MapUtils.getZFromID(id) + ".nbt");

            try {
                // FileOutputStream f = new FileOutputStream(file);
                // f.write(data);
                // f.close();
                NbtIo.write(output, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getColor(int x, int z) {
        MapChunk chunk = getChunk(x >> 4, z >> 4);
        return chunk.getColor(x & 15, z & 15);
    }

    public void tick() {
        if (timeToUpdate.size() > 0) {
            synchronized (timeToUpdate) {
                // Set<Chunk> chunks = new HashSet<Chunk>();
                Set<ChunkAccess> chunks = new HashSet<ChunkAccess>();
                chunks.addAll(timeToUpdate.keySet());
                // for (Chunk c : chunks)
                for (ChunkAccess c : chunks) {
                    int v = timeToUpdate.get(c);
                    if (v > 1) {
                        timeToUpdate.put(c, v - 1);
                    } else {
                        try {
                            // updateChunk(c);
                            if (c instanceof LevelChunk) {
                                updateChunk((LevelChunk) c);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void updateChunk(LevelChunk rchunk) {
        // long id = MapUtils.getIDFromCoords(rchunk.xPosition, rchunk.zPosition);
        long id = MapUtils.getIDFromCoords(rchunk.getPos().x, rchunk.getPos().z);
        // MapChunk chunk = getChunk(rchunk.xPosition, rchunk.zPosition);
        MapChunk chunk = getChunk(rchunk.getPos().x, rchunk.getPos().z);
        chunk.update(rchunk);
        updatedChunks.add(id);
        synchronized (timeToUpdate) {
            timeToUpdate.remove(rchunk);
        }
        regionUpdateTime.put(id, (new Date()).getTime());
    }

    public long getUpdateTime(int x, int z) {
        return regionUpdateTime.get(MapUtils.getIDFromCoords(x, z));
    }

    public void updateChunkDelayed(ChunkAccess chunk, byte time) {
        synchronized (timeToUpdate) {
            timeToUpdate.put(chunk, (int) time);
        }
    }
}
