package buildcraft.robotics.path;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import net.minecraft.util.BlockPos;

import buildcraft.api.core.BCLog;

public class MiniChunkCalculationData {
    public enum CalculationStep {
        /** Constructing state */
        REQUESTED,
        /** Filling out the expenses from the world */
        FILLING,
        /** Intermediate state after {@link #FILLING} and before {@link #ANALYSING} */
        FILLED,
        /** Finding out which parts of the minichunk connect to each other. This is the most expensive step. */
        ANALYSING,
        /** Intermediate state after {@link #ANALYSING} and before {@link #CREATING_NODES} */
        ANALYSED,
        /** Creates nodes for the minichunk. */
        CREATING_NODES,
        /** Intermediate state after {@link #CREATING_NODES} and before {@link #JOINING_AROUND} */
        HAS_NODES,
        /** Joins this minichunk with other existing minichunk graphs. */
        JOINING_AROUND,
        /** Finished state */
        COMPLETE;
    }

    public final MiniChunkCache cache;
    public final BlockPos min;
    public final CompletableFuture<MiniChunkGraph> futureResult = new CompletableFuture<>();

    final byte[][][] expenseArray = new byte[16][16][16];
    // We need 4+4+4 bits to store- so a short
    final short[][][] graphArray = new short[16][16][16];

    volatile int numNodes = 0;
    volatile boolean hasNonAir = false;
    volatile CalculationStep step = CalculationStep.REQUESTED;
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

    void step(CalculationStep expected, CalculationStep to) {
        if (step != expected) {
            throw new IllegalStateException("Wrong State! Expected " + expected + " but found " + step);
        }
        step = to;
        long now = System.currentTimeMillis();
        long diff = now - lastTime;
        lastTime = now;
        BCLog.logger.info(min + " [step] Advancing from " + expected + " to " + to + ", took " + diff + "ms");
    }
}
