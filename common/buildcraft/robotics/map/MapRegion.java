package buildcraft.robotics.map;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;

// public class MapRegion implements INBTStoreable
public class MapRegion {
    // private final IntHashMap chunks = new IntHashMap();
    private final Int2ObjectOpenHashMap<MapChunk> chunks = new Int2ObjectOpenHashMap<>();
    private final int x, z;

    public MapRegion(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean hasChunk(int x, int z) {
        // return chunks.containsItem((z << 4) | x);
        return chunks.containsKey((z << 4) | x);
    }

    public MapChunk getChunk(int x, int z) {
        int id = (z << 4) | x;
        // MapChunk chunk = (MapChunk) chunks.lookup(id);
        MapChunk chunk = (MapChunk) chunks.get(id);
        if (chunk == null) {
            chunk = new MapChunk(x, z);
            // chunks.addKey(id, chunk);
            chunks.put(id, chunk);
        }
        return chunk;
    }

    // @Override
    public void readFromNBT(CompoundTag tag) {
        // chunks.clearMap();
        chunks.clear();

        if (tag != null) {
            for (int i = 0; i < 256; i++) {
                if (tag.contains("r" + i)) {
                    MapChunk chunk = new MapChunk(tag.getCompound("r" + i));
                    // chunks.addKey(i, chunk);
                    chunks.put(i, chunk);
                }
            }
        }
    }

    // @Override
    public void writeToNBT(CompoundTag tag) {
        for (int i = 0; i < 256; i++) {
            // MapChunk chunk = (MapChunk) chunks.lookup(i);
            MapChunk chunk = (MapChunk) chunks.get(i);
            if (chunk != null) {
                CompoundTag chunkNBT = new CompoundTag();
                synchronized (chunk) {
                    chunk.writeToNBT(chunkNBT);
                }
                tag.put("r" + i, chunkNBT);
            }
        }
    }
}
