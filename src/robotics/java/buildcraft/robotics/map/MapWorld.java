package buildcraft.robotics.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import buildcraft.core.lib.utils.NBTUtils;

import gnu.trove.map.hash.TLongObjectHashMap;

public class MapWorld {
    private final World world;
    private final TLongObjectHashMap<MapRegion> regionMap;
    private final Set<QueuedXZ> regionUpdateSet = new HashSet<QueuedXZ>();
    private final Queue<QueuedXZ> queuedChunks;
    private final File location;

    private long lastForcedChunkLoad;

    private class QueuedXZ {
        int x, z, p;

        QueuedXZ(int x, int z, int p) {
            this.x = x;
            this.z = z;
            this.p = p;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof QueuedXZ)) {
                return false;
            }
            return ((QueuedXZ) other).x == x && ((QueuedXZ) other).z == z;
        }

        @Override
        public int hashCode() {
            return x * 31 + z;
        }
    }

    public MapWorld(World world, File location) {
        this.world = world;
        regionMap = new TLongObjectHashMap<MapRegion>();
        queuedChunks = new PriorityQueue<QueuedXZ>(11, new Comparator<QueuedXZ>() {
            @Override
            public int compare(QueuedXZ c1, QueuedXZ c2) {
                return (c1 != null ? c1.p : 0) - (c2 != null ? c2.p : 0);
            }
        });

        String saveFolder = world.provider.getSaveFolder();
        if (saveFolder == null) {
            saveFolder = "world";
        }
        this.location = new File(location, saveFolder);
        try {
            this.location.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long getXzId(int x, int z) {
        return (x << 24) | z;
    }

    private MapRegion getRegion(int x, int z) {
        long id = getXzId(x, z);
        MapRegion region = regionMap.get(id);
        if (region == null) {
            region = new MapRegion(x, z);

            // Check in the location first
            File target = new File(location, "r" + x + "," + z + ".nbt");
            if (target.exists()) {
                try {
                    FileInputStream f = new FileInputStream(target);
                    byte[] data = new byte[(int) target.length()];
                    f.read(data);
                    f.close();

                    region.readFromNBT(NBTUtils.load(data));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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

    public void queueChunkForUpdate(int x, int z, int priority) {
        long id = getXzId(x, z);
        queuedChunks.add(new QueuedXZ(x, z, priority));
    }

    public void queueChunkForUpdateIfEmpty(int x, int z, int priority) {
        if (!hasChunk(x, z)) {
            queueChunkForUpdate(x, z, priority);
        }
    }

    public void updateChunkInQueue() {
        if (queuedChunks.size() == 0) {
            return;
        }

        QueuedXZ q = queuedChunks.remove();
        if (q == null) {
            return;
        }

        if (!world.getChunkProvider().chunkExists(q.x, q.z)) {
            long now = (new Date()).getTime();
            if (now - lastForcedChunkLoad < 1000) {
                q.p++; // Increase priority so it gets looked at later
                queuedChunks.add(q);
                return;
            } else {
                lastForcedChunkLoad = now;
            }
        }

        updateChunk(q.x, q.z);
    }

    public void save() {
        Iterator<QueuedXZ> i = regionUpdateSet.iterator();

        while (i.hasNext()) {
            QueuedXZ id = i.next();
            i.remove();
            if (id == null) {
                continue;
            }

            MapRegion region = regionMap.get(getXzId(id.x, id.z));
            if (region == null) {
                continue;
            }

            NBTTagCompound output = new NBTTagCompound();
            region.writeToNBT(output);
            byte[] data = NBTUtils.save(output);
            File file = new File(location, "r" + id.x + "," + id.z + ".nbt");

            try {
                FileOutputStream f = new FileOutputStream(file);
                f.write(data);
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getColor(int x, int z) {
        MapChunk chunk = getChunk(x >> 4, z >> 4);
        return chunk.getColor(x & 15, z & 15);
    }

    protected void updateChunk(int x, int z) {
        MapChunk chunk = getChunk(x, z);
        chunk.update(world.getChunkFromChunkCoords(x, z));
        regionUpdateSet.add(new QueuedXZ(x >> 4, z >> 4, 0));

        // priority does not matter - see equals
        queuedChunks.remove(new QueuedXZ(x, z, 0));
    }
}
