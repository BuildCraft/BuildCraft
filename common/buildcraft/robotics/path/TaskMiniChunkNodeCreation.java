package buildcraft.robotics.path;

import buildcraft.core.lib.BCWorkerThreads;
import buildcraft.robotics.path.MiniChunkGraph.ChunkType;

public class TaskMiniChunkNodeCreation implements Runnable {
    private final MiniChunkCalculationData data;

    public TaskMiniChunkNodeCreation(MiniChunkCalculationData data) {
        this.data = data;
    }

    @Override
    public void run() {
        synchronized (data) {
            createNodes();
        }
        BCWorkerThreads.executeWorkTask(new TaskMiniChunkJoiner(data));
    }

    private void createNodes() {
        ChunkType type;
        if (data.numNodes == 0) {
            type = ChunkType.COMPLETLY_FILLED;
        } else if (data.numNodes == 1) {
            if (data.hasNonAir) {
                type = ChunkType.SINGLE_GRAPH;
            } else {
                type = ChunkType.COMPLETLY_FREE;
            }
        } else {
            type = ChunkType.MULTIPLE_GRAPHS;
        }
        byte[][][] graphArray = new byte[16][16][16];

        data.graph = new MiniChunkGraph(data.min, type, data.expenseArray, graphArray, data.numNodes);
        data.cache.putGraph(data.min, data.graph);
    }
}
