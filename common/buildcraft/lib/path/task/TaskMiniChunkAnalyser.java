/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.path.task;

import buildcraft.lib.path.task.AnalysedChunk.MiniGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class TaskMiniChunkAnalyser implements Callable<AnalysedChunk> {
    private final FilledChunk filled;

    public TaskMiniChunkAnalyser(FilledChunk filled) {
        this.filled = filled;
    }

    @Override
    public AnalysedChunk call() {
        AnalysedChunk chunk = new AnalysedChunk(filled);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    MiniGraph g = chunk.graphs[x][y][z];
                    EnumTraversalExpense exp = chunk.expenses[x][y][z];
                    if (exp != EnumTraversalExpense.SOLID & g == null) {
                        spreadGraph(chunk, x, y, z);
                    }
                }
            }
        }
        return chunk;
    }

    private static void spreadGraph(AnalysedChunk chunk, int x, int y, int z) {
        Set<BlockPos> openSet = new HashSet<>();
        Set<BlockPos> closedSet = new HashSet<>();
        MiniGraph graph = new MiniGraph();
        openSet.add(new BlockPos(x, y, z));
        while (!openSet.isEmpty()) {
            BlockPos toTest = openSet.iterator().next();
            int x_ = toTest.getX();
            int y_ = toTest.getY();
            int z_ = toTest.getZ();
            EnumTraversalExpense expense = chunk.expenses[x_][y_][z_];
            if (expense != EnumTraversalExpense.SOLID) {
                graph.blockCount++;
                graph.totalExpense += expense.expense;
                chunk.graphs[x_][y_][z_] = graph;
                for (Direction face : Direction.values()) {
                    BlockPos offset = toTest.relative(face);
                    if (!isValid(offset)) continue;
                    if (closedSet.contains(offset)) continue;
                    openSet.add(offset);
                }
                openSet.remove(toTest);
                closedSet.add(toTest);
            }
        }
    }

    public static boolean isValid(BlockPos offset) {
        if (offset.getX() < 0 || offset.getX() >= 16) return false;
        if (offset.getY() < 0 || offset.getY() >= 16) return false;
        if (offset.getZ() < 0 || offset.getZ() >= 16) return false;
        return true;
    }
}
