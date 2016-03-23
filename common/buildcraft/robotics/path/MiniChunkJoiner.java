package buildcraft.robotics.path;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.robotics.path.MiniChunkCalculationData.CalculationStep;

public class MiniChunkJoiner implements Runnable {
    private final MiniChunkCalculationData data;

    public MiniChunkJoiner(MiniChunkCalculationData data) {
        this.data = data;
    }

    @Override
    public void run() {
        if (data.step != CalculationStep.HAS_NODES) {
            throw new IllegalStateException("Wrong state! Expected HAS_NODES but found " + data.step + " [no-sync]");
        }
        synchronized (data) {
            if (data.step != CalculationStep.HAS_NODES) {
                throw new IllegalStateException("Wrong state! Expected HAS_NODES but found " + data.step + " [synchronized]");
            }
            data.step = CalculationStep.JOINING_AROUND;
            join();
            data.step = CalculationStep.COMPLETE;
        }
        data.futureResult.complete(data.graph);
    }

    private void join() {
        MiniChunkGraph graph = data.graph;
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos offsetMin = data.min.offset(face, 16);
            MiniChunkGraph offset = data.cache.getGraph(offsetMin);
            if (offset != null) {
                graph.neighbours.put(face, offset);
            }
        }

        for (EnumFacing face : EnumFacing.values()) {
            BlockPos min = null;
            BlockPos max = null;
            // TODO: THIS!
            for (BlockPos inside : BlockPos.getAllInBox(min, max)) {

            }
        }
    }
}
