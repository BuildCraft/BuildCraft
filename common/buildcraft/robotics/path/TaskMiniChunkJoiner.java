package buildcraft.robotics.path;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.robotics.path.MiniChunkCalculationData.CalculationStep;
import buildcraft.robotics.path.MiniChunkGraph.MiniChunkNode;

public class TaskMiniChunkJoiner implements Runnable {
    public static final BlockPos[][] corners = { // Packed array
        { new BlockPos(0, 0, 0), new BlockPos(15, 0, 15) },// DOWN (-Y)
        { new BlockPos(0, 15, 0), new BlockPos(15, 15, 15) },// UP (+Y)
        { new BlockPos(0, 0, 0), new BlockPos(15, 15, 0) },// NORTH (-Z)
        { new BlockPos(0, 0, 15), new BlockPos(15, 15, 15) },// SOUTH (+Z)
        { new BlockPos(0, 0, 0), new BlockPos(0, 15, 15) },// WEST (-X)
        { new BlockPos(15, 0, 0), new BlockPos(15, 15, 15) },// EAST (+X)
    };

    private final MiniChunkCalculationData data;

    public TaskMiniChunkJoiner(MiniChunkCalculationData data) {
        this.data = data;
    }

    @Override
    public void run() {
        synchronized (data) {
            data.step(CalculationStep.HAS_NODES, CalculationStep.JOINING_AROUND);
            join();
            data.step(CalculationStep.JOINING_AROUND, CalculationStep.COMPLETE);
        }
        data.futureResult.complete(data.graph);
    }

    private void join() {
        MiniChunkGraph graph = data.graph;
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos offsetMin = data.min.offset(face, 16);
            MiniChunkGraph offset = data.cache.getGraphIfExistsImpl(offsetMin);
            if (offset != null) {
                graph.neighbours.put(face, offset);
            }
        }

        for (EnumFacing face : EnumFacing.values()) {
            if (!graph.neighbours.containsKey(face)) continue;
            MiniChunkGraph neighbour = graph.neighbours.get(face);
            BlockPos min = corners[face.ordinal()][0];
            BlockPos max = corners[face.ordinal()][1];
            for (BlockPos inside : BlockPos.getAllInBox(min, max)) {
                int thisGraph = graph.graphArray[inside.getX()][inside.getY()][inside.getZ()];
                if (thisGraph < 0) continue;
                BlockPos neighbourPos = wrap(inside.offset(face));
                int neighbourGraph = neighbour.graphArray[neighbourPos.getX()][neighbourPos.getY()][neighbourPos.getZ()];
                if (neighbourGraph < 0) continue;
                MiniChunkNode thisNode = graph.nodes.get(thisGraph);
                MiniChunkNode neighbourNode = neighbour.nodes.get(neighbourGraph);
                thisNode.connected.add(neighbourNode);
                neighbourNode.connected.add(thisNode);
            }
        }
    }

    public static BlockPos wrap(BlockPos pos) {
        int x = (pos.getX() % 16 + 16) % 16;
        int y = (pos.getY() % 16 + 16) % 16;
        int z = (pos.getZ() % 16 + 16) % 16;
        return new BlockPos(x, y, z);
    }
}
