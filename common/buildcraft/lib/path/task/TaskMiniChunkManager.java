package buildcraft.lib.path.task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.WorkerThreadUtil;
import buildcraft.lib.path.MiniChunkGraph;

public class TaskMiniChunkManager implements Callable<MiniChunkGraph> {
    private World world;
    private final BlockPos offset;
    private final Consumer<MiniChunkGraph> setter;

    public TaskMiniChunkManager(World world, BlockPos offset, Consumer<MiniChunkGraph> setter) {
        this.world = world;
        this.offset = offset;
        this.setter = setter;
    }

    private static <T> T execute(Callable<T> callable) throws InterruptedException {
        return WorkerThreadUtil.executeWorkTaskWaiting(callable);
    }

    @Override
    public MiniChunkGraph call() throws Exception {
        try {
            FilledChunk filled = execute(new TaskMiniChunkFiller(world, offset));
            world = null;// We no longer need this. Let the GC remove this if we are holding the last reference to it.
            AnalysedChunk analysed = execute(new TaskMiniChunkAnalyser(filled));
            MiniChunkGraph graph = null;
            setter.accept(graph);
            return graph;
        } catch (InterruptedException ex) {
            throw ex;
        }
    }
}
