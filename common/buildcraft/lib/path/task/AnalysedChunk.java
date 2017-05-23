/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.path.task;

public class AnalysedChunk {
    public final int[] expenseCounts;
    public final EnumTraversalExpense[][][] expenses;
    public final MiniGraph[][][] graphs = new MiniGraph[16][16][16];

    public AnalysedChunk(FilledChunk filled) {
        expenseCounts = filled.expenseCounts;
        expenses = filled.expenses;
    }

    public static class MiniGraph {
        public int blockCount, totalExpense;
    }
}
