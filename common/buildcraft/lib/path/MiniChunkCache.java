/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.path;

import buildcraft.lib.misc.WorkerThreadUtil;
import buildcraft.lib.path.task.TaskMiniChunkManager;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Futures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class MiniChunkCache {
    // private static Map<Integer, MiniChunkCache> worldCaches = new HashMap<>();
    private static Map<ResourceKey<Level>, MiniChunkCache> worldCaches = new HashMap<>();

    // public final int dimId;
    public final ResourceKey<Level> dimId;
    private final Map<BlockPos, MiniChunkGraph> cache = new ConcurrentHashMap<>();
    final Map<BlockPos, Future<MiniChunkGraph>> tempData = new ConcurrentHashMap<>();

    // private MiniChunkCache(int dimId)
    private MiniChunkCache(ResourceKey<Level> dimId) {
        this.dimId = dimId;
    }

    public static Future<MiniChunkGraph> requestGraph(Level world, BlockPos pos) {
//        int dimId = world.provider.getDimension();
        ResourceKey<Level> dimId = world.dimension();
        if (!worldCaches.containsKey(dimId)) {
            worldCaches.put(dimId, new MiniChunkCache(dimId));
        }
        return worldCaches.get(dimId).requestGraphImpl(world, pos);
    }

    public static MiniChunkGraph getGraphIfExists(Level world, BlockPos pos) {
//        int dimId = world.provider.getDimension();
        ResourceKey<Level> dimId = world.dimension();
        if (!worldCaches.containsKey(dimId)) {
            worldCaches.put(dimId, new MiniChunkCache(dimId));
        }
        return worldCaches.get(dimId).getGraphIfExistsImpl(pos);
    }

    public static MiniChunkGraph requestAndWait(Level world, BlockPos pos) {
        try {
            return requestGraph(world, pos).get();
        } catch (InterruptedException | ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    private void putGraph(BlockPos min, MiniChunkGraph graph) {
        cache.put(min, graph);
    }

    private Future<MiniChunkGraph> requestGraphImpl(Level world, BlockPos pos) {
        final BlockPos minPos = convertToMin(pos);
        pos = minPos;
        MiniChunkGraph existing = cache.get(pos);
        if (existing != null) {
//            return Futures.immediateCheckedFuture(existing);
            return Futures.immediateFuture(existing);
        }
//        if (!world.isBlockLoaded(pos))
        if (!world.isLoaded(pos))
            return Futures.immediateFailedFuture(new Throwable("The block " + pos + " is not loaded!"));
        synchronized (this) {
            if (tempData.containsKey(pos)) {
                return tempData.get(pos);
            }
            Consumer<MiniChunkGraph> setter = (graph) -> putGraph(minPos, graph);
            Callable<MiniChunkGraph> task = new TaskMiniChunkManager(world, pos, setter);
            Future<MiniChunkGraph> future = WorkerThreadUtil.executeDependantTask(task);
            tempData.put(pos, future);
            return future;
        }
    }

    private MiniChunkGraph getGraphIfExistsImpl(BlockPos pos) {
        pos = convertToMin(pos);
        return cache.get(pos);
    }

    private static BlockPos convertToMin(BlockPos pos) {
        // Get the minimum blockpos of the minichunk
        int x = (pos.getX() / 16) * 16;
        int y = (pos.getY() / 16) * 16;
        int z = (pos.getZ() / 16) * 16;
        return new BlockPos(x, y, z);
    }
}
