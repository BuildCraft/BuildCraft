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
