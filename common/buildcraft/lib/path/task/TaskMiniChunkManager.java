/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.path.task;

import buildcraft.lib.misc.WorkerThreadUtil;
import buildcraft.lib.path.MiniChunkGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class TaskMiniChunkManager implements Callable<MiniChunkGraph> {
    private Level world;
    private final BlockPos offset;
    private final Consumer<MiniChunkGraph> setter;

    public TaskMiniChunkManager(Level world, BlockPos offset, Consumer<MiniChunkGraph> setter) {
        this.world = world;
        this.offset = offset;
        this.setter = setter;
    }

    private static <T> T execute(Callable<T> callable) throws InterruptedException {
        return WorkerThreadUtil.executeWorkTaskWaiting(callable);
    }

    @Override
    public MiniChunkGraph call() throws Exception {
        FilledChunk filled = execute(new TaskMiniChunkFiller(world, offset));
        world = null;// We no longer need this. Let the GC remove this if we are holding the last reference to it.
        AnalysedChunk analysed = execute(new TaskMiniChunkAnalyser(filled));
        MiniChunkGraph graph = null;
        setter.accept(graph);
        return graph;
    }
}
