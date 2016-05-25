package buildcraft.lib.path.task;

import java.util.concurrent.Callable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.WorkerThreadUtil;
import buildcraft.lib.path.MiniChunkGraph;

public class TaskMiniChunkManager implements Callable<MiniChunkGraph> {
    private World world;
    private final BlockPos offset;

    public TaskMiniChunkManager(World world, BlockPos offset) {
        this.world = world;
        this.offset = offset;
    }

    public static <T> T execute(Callable<T> callable) throws InterruptedException {
        return WorkerThreadUtil.executeWorkTaskWaiting(callable);
    }

    @Override
    public MiniChunkGraph call() throws Exception {
        try {
            FilledChunk filled = execute(new TaskMiniChunkFiller(world, offset));
            world = null;

            return null;
        } catch (InterruptedException ex) {
            throw ex;
        }
    }
}
