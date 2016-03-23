package buildcraft.robotics.path;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.util.BlockPos;

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
        /** Creates nodes for the minichunk.*/
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

    /** Data changes depending on what stage it is currently at. */
    final int[][][] blockData = new int[16][16][16];

    volatile CalculationStep step = CalculationStep.REQUESTED;
    volatile MiniChunkGraph graph;
    final List<Set<BlockPos>> joinedGraphs = new CopyOnWriteArrayList<>();

    public MiniChunkCalculationData(MiniChunkCache cache, BlockPos min) {
        this.cache = cache;
        this.min = min;
    }
}
