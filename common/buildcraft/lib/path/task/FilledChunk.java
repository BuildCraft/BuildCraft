package buildcraft.lib.path.task;

public class FilledChunk {
    public final ExpenseType[][][] expenses = new ExpenseType[16][16][16];
    public final int[] expenseCounts = new int[3];

    public enum ExpenseType {
        AIR((byte) 1),
        FLUID((byte) 3),
        SOLID((byte) 63);

        public final byte expense;

        private ExpenseType(byte expense) {
            this.expense = expense;
        }
    }
}
