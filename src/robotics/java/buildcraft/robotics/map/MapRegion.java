package buildcraft.robotics.map;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.core.INBTStoreable;

import gnu.trove.map.hash.TIntObjectHashMap;

public class MapRegion implements INBTStoreable {
    private final TIntObjectHashMap<MapChunk> chunks = new TIntObjectHashMap<MapChunk>();
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
        return chunks.contains((z << 4) | x);
    }

    public MapChunk getChunk(int x, int z) {
        int id = (z << 4) | x;
        MapChunk chunk = chunks.get(id);
        if (chunk == null) {
            chunk = new MapChunk(x, z);
            chunks.put(id, chunk);
        }
        return chunk;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        chunks.clear();

        for (int i = 0; i < 256; i++) {
            if (tag.hasKey("r" + i)) {
                MapChunk chunk = new MapChunk(tag.getCompoundTag("r" + i));
                chunks.put(i, chunk);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        for (int i = 0; i < 256; i++) {
            MapChunk chunk = chunks.get(i);
            if (chunk != null) {
                NBTTagCompound chunkNBT = new NBTTagCompound();
                chunk.writeToNBT(chunkNBT);
                tag.setTag("r" + i, chunkNBT);
            }
        }
    }
}
