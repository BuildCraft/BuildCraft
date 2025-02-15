/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.path.task;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.concurrent.Callable;

public class TaskMiniChunkFiller implements Callable<FilledChunk> {
    private final Level world;
    private final BlockPos offset;

    public TaskMiniChunkFiller(Level world, BlockPos min) {
        this.world = world;
        this.offset = min;
    }

    @Override
    public FilledChunk call() throws Exception {
        FilledChunk filled = new FilledChunk();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = offset.offset(x, y, z);
                    EnumTraversalExpense expense = EnumTraversalExpense.getFor(world, pos, world.getBlockState(pos));
                    filled.expenses[x][y][z] = expense;
                    filled.expenseCounts[expense.ordinal()]++;
                }
            }
        }
        return filled;
    }
}
