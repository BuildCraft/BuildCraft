package buildcraft.lib.path.task;

import java.util.concurrent.Callable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.path.task.FilledChunk.ExpenseType;

public class TaskMiniChunkFiller implements Callable<FilledChunk> {
    private final World world;
    private final BlockPos offset;

    public TaskMiniChunkFiller(World world, BlockPos min) {
        this.world = world;
        this.offset = min;
    }

    @Override
    public FilledChunk call() throws Exception {
        FilledChunk filled = new FilledChunk();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = offset.add(x, y, z);
                    ExpenseType expense = getExpenseFromWorld(pos);
                    filled.expenses[x][y][z] = expense;
                    filled.expenseCounts[expense.ordinal()]++;
                }
            }
        }
        return filled;
    }

    private ExpenseType getExpenseFromWorld(BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (world.isAirBlock(pos)) {
            return ExpenseType.AIR;
        }
        Material material = state.getMaterial();
        if (material.isLiquid()) {
            return ExpenseType.FLUID;
        }
        if (!material.isSolid()) {
            return ExpenseType.AIR;
        }
        return ExpenseType.SOLID;
    }
}
