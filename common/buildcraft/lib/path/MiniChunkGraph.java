/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.path;

import buildcraft.lib.path.task.TaskMiniChunkAnalyser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class MiniChunkGraph {
    public enum ChunkType {
        COMPLETELY_FREE,
        SINGLE_GRAPH,
        MULTIPLE_GRAPHS,
        COMPLETELY_FILLED
    }

    public final BlockPos min;
    public final ChunkType type;
    public final Map<Direction, MiniChunkGraph> neighbours = new EnumMap<>(Direction.class);
    public final ImmutableList<MiniChunkNode> nodes;
    final byte[][][] expenseArray, graphArray;

    public MiniChunkGraph(BlockPos min, ChunkType type, byte[][][] expenseArray, byte[][][] graphArray, int numNodes) {
        this.min = min;
        this.type = type;
        this.expenseArray = expenseArray;
        this.graphArray = graphArray;
        ImmutableList.Builder<MiniChunkNode> builder = ImmutableList.builder();
        for (int i = 0; i < numNodes; i++) {
            builder.add(new MiniChunkNode(i));
        }
        this.nodes = builder.build();
    }

    public MiniChunkNode getFor(BlockPos pos) {
        BlockPos normalised = pos.subtract(min);
        if (!TaskMiniChunkAnalyser.isValid(normalised))
            throw new IllegalArgumentException("The position " + normalised + " was invalid! (from " + pos + ")");
        int id = graphArray[normalised.getX()][normalised.getY()][normalised.getZ()];
        if (id >= 0) return nodes.get(id);
        throw new IllegalArgumentException("The position " + normalised + " had no graph! (gId = " + id + ")");
    }

    public class MiniChunkNode {
        public final int id;
        final Set<MiniChunkNode> connected = Sets.newIdentityHashSet();

        public MiniChunkNode(int id) {
            this.id = id;
        }

        public Set<MiniChunkNode> getConnected() {
            return Collections.unmodifiableSet(connected);
        }

        /** Checks if this node contains the given position. This will be the world position of the block */
        public boolean contains(BlockPos pos) {
            BlockPos normalised = pos.subtract(min);
            if (!TaskMiniChunkAnalyser.isValid(normalised)) return false;
            return graphArray[normalised.getX()][normalised.getY()][normalised.getZ()] == id;
        }

        public int getExpense(BlockPos pos) {
            BlockPos normalised = pos.subtract(min);
            if (!TaskMiniChunkAnalyser.isValid(normalised)) return Integer.MAX_VALUE;
            int expense = expenseArray[normalised.getX()][normalised.getY()][normalised.getZ()];
            if (expense < 0) return Integer.MAX_VALUE;
            return expense;
        }

        public MiniChunkGraph getParent() {
            return MiniChunkGraph.this;
        }

        public void requestAllConnected(World world) {
            // Request all first, and THEN wait for all of them.
            for (Direction face : Direction.values()) {
                if (!neighbours.containsKey(face)) {
                    MiniChunkCache.requestGraph(world, min.relative(face, 16));
                }
            }
            for (Direction face : Direction.values()) {
                if (!neighbours.containsKey(face)) {
                    MiniChunkCache.requestAndWait(world, min.relative(face, 16));
                }
            }
        }
    }
}
