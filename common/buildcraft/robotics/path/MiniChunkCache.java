package buildcraft.robotics.path;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.Futures;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MiniChunkCache {
    static final Executor WORKER_THREAD_POOL;

    static {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors <= 3) {
            WORKER_THREAD_POOL = Executors.newSingleThreadExecutor();
        } else {
            // Got a fast processor? You shall have fast robots.
            WORKER_THREAD_POOL = Executors.newFixedThreadPool(2);
        }
    }

    private static Map<Integer, MiniChunkCache> worldCaches = new HashMap<>();

    public final int dimId;
    private final Map<BlockPos, MiniChunkGraph> miniChunkCache = new ConcurrentHashMap<>();

    public MiniChunkCache(int dimId) {
        this.dimId = dimId;
    }

    void putGraph(BlockPos min, MiniChunkGraph graph) {
        miniChunkCache.put(min, graph);
    }

    public Future<MiniChunkGraph> requestGraph(World world, BlockPos pos) {
        pos = convertToMin(pos);
        MiniChunkGraph existing = miniChunkCache.get(pos);
        if (existing != null) {
            return Futures.immediateCheckedFuture(existing);
        }
        MiniChunkCalculationData data = new MiniChunkCalculationData(this, pos);
        // Fill the data (from the world) in a separate thread
        WORKER_THREAD_POOL.execute(new MiniChunkFiller(world, data));
        return data.futureResult;
    }

    public MiniChunkGraph getGraph(BlockPos pos) {
        pos = convertToMin(pos);
        return miniChunkCache.get(pos);
    }

    private static BlockPos convertToMin(BlockPos pos) {
        // Get the minimum blockpos of the minichunk
        int x = (pos.getX() / 16) * 16;
        int y = (pos.getY() / 16) * 16;
        int z = (pos.getZ() / 16) * 16;
        return new BlockPos(x, y, z);
    }
}
