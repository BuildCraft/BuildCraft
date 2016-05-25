package buildcraft.lib.path.task;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.minecraft.util.math.BlockPos;

import buildcraft.lib.path.MiniChunkCache;
import buildcraft.lib.path.MiniChunkGraph;

public class MiniChunkCalculationData {
    public final MiniChunkCache cache;
    public final BlockPos min;
    public final CompletableFuture<MiniChunkGraph> futureResult = new CompletableFuture<>();

    final byte[][][] expenseArray = new byte[16][16][16];
    // We need 4+4+4 bits to store- so a short
    final short[][][] graphArray = new short[16][16][16];

    volatile int numNodes = 0;
    volatile boolean hasNonAir = false;
    volatile MiniChunkGraph graph;
    volatile long lastTime = System.currentTimeMillis();

    public MiniChunkCalculationData(MiniChunkCache cache, BlockPos min) {
        this.cache = cache;
        this.min = min;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                Arrays.fill(graphArray[x][y], (short) -1);
            }
        }
    }
}
