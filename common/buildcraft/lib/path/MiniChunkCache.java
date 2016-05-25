package buildcraft.lib.path;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Futures;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.WorkerThreadUtil;
import buildcraft.lib.path.task.MiniChunkCalculationData;
import buildcraft.lib.path.task.TaskMiniChunkManager;

public class MiniChunkCache {
    private static Map<Integer, MiniChunkCache> worldCaches = new HashMap<>();

    public final int dimId;
    private final Map<BlockPos, MiniChunkGraph> cache = new ConcurrentHashMap<>();
    final Map<BlockPos, MiniChunkCalculationData> tempData = new ConcurrentHashMap<>();

    private MiniChunkCache(int dimId) {
        this.dimId = dimId;
    }

    public static Future<MiniChunkGraph> requestGraph(World world, BlockPos pos) {
        int dimId = world.provider.getDimension();
        if (!worldCaches.containsKey(dimId)) {
            worldCaches.put(dimId, new MiniChunkCache(dimId));
        }
        return worldCaches.get(dimId).requestGraphImpl(world, pos);
    }

    public static MiniChunkGraph getGraphIfExists(World world, BlockPos pos) {
        int dimId = world.provider.getDimension();
        if (!worldCaches.containsKey(dimId)) {
            worldCaches.put(dimId, new MiniChunkCache(dimId));
        }
        return worldCaches.get(dimId).getGraphIfExistsImpl(pos);
    }

    public static MiniChunkGraph requestAndWait(World world, BlockPos pos) {
        try {
            return requestGraph(world, pos).get();
        } catch (InterruptedException | ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    void putGraph(BlockPos min, MiniChunkGraph graph) {
        cache.put(min, graph);
    }

    Future<MiniChunkGraph> requestGraphImpl(World world, BlockPos pos) {
        pos = convertToMin(pos);
        MiniChunkGraph existing = cache.get(pos);
        if (existing != null) {
            return Futures.immediateCheckedFuture(existing);
        }
        if (!world.isBlockLoaded(pos)) return Futures.immediateFailedFuture(new Throwable("The block " + pos + " is not loaded!"));
        synchronized (this) {
            if (tempData.containsKey(pos)) {
                return tempData.get(pos).futureResult;
            }
            MiniChunkCalculationData data = new MiniChunkCalculationData(this, pos);
            tempData.put(pos, data);// FIXME: This uses the wrong data object!
            // Fill the data (from the world) in the thread pool
            WorkerThreadUtil.executeWorkTask(new TaskMiniChunkManager(world, pos));
            return data.futureResult;
        }
    }

    MiniChunkGraph getGraphIfExistsImpl(BlockPos pos) {
        pos = convertToMin(pos);
        return cache.get(pos);
    }

    private static BlockPos convertToMin(BlockPos pos) {
        // Get the minimum blockpos of the minichunk
        int x = (pos.getX() / 16) * 16;
        int y = (pos.getY() / 16) * 16;
        int z = (pos.getZ() / 16) * 16;
        return new BlockPos(x, y, z);
    }
}
