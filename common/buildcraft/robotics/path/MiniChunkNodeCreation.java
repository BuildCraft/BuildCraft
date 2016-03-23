package buildcraft.robotics.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.util.BlockPos;

import buildcraft.robotics.path.MiniChunkCalculationData.CalculationStep;
import buildcraft.robotics.path.MiniChunkGraph.AirNode;
import buildcraft.robotics.path.MiniChunkGraph.ChunkType;
import buildcraft.robotics.path.MiniChunkGraph.MultiNode;
import buildcraft.robotics.path.MiniChunkGraph.NodeBase;

public class MiniChunkNodeCreation implements Runnable {
    private final MiniChunkCalculationData data;

    public MiniChunkNodeCreation(MiniChunkCalculationData data) {
        this.data = data;
    }

    @Override
    public void run() {
        if (data.step != CalculationStep.ANALYSED) {
            throw new IllegalStateException("Wrong state! Expected ANALYSED but found " + data.step + " [no-sync]");
        }
        synchronized (data) {
            if (data.step != CalculationStep.ANALYSED) {
                throw new IllegalStateException("Wrong state! Expected ANALYSED but found " + data.step + " [synchronized]");
            }
            data.step = CalculationStep.CREATING_NODES;
            createNodes();
            data.step = CalculationStep.HAS_NODES;
        }
        MiniChunkCache.WORKER_THREAD_POOL.execute(new MiniChunkJoiner(data));
    }

    private void createNodes() {
        ChunkType type;
        List<NodeBase> nodes = new ArrayList<>();
        if (data.joinedGraphs.isEmpty()) {
            type = ChunkType.COMPLETLY_FILLED;
        } else if (data.joinedGraphs.size() == 1) {
            Set<BlockPos> positions = data.joinedGraphs.get(0);
            if (positions.size() == 16 * 16 * 16) {
                type = ChunkType.COMPLETLY_FREE;
                nodes.add(new AirNode(data.min));
            } else {
                type = ChunkType.SINGLE_GRAPH;
                nodes.add(new MultiNode(data.min, data.joinedGraphs.get(0)));
            }
        } else {
            type = ChunkType.MULTIPLE_GRAPHS;
            for (Set<BlockPos> joined : data.joinedGraphs) {
                nodes.add(new MultiNode(data.min, joined));
            }
        }

        data.graph = new MiniChunkGraph(type, ImmutableList.copyOf(nodes));
        data.cache.putGraph(data.min, data.graph);
    }
}
