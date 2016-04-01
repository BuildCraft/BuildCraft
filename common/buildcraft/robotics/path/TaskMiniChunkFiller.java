package buildcraft.robotics.path;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.BCWorkerThreads;

public class TaskMiniChunkFiller implements Runnable {
    public static final int EXPENSE_AIR = 1;
    public static final int EXPENSE_FLUID = 3;

    private final World world;
    private final MiniChunkCalculationData data;

    public TaskMiniChunkFiller(World world, MiniChunkCalculationData data) {
        this.world = world;
        this.data = data;
    }

    @Override
    public void run() {
        synchronized (data) {
            fill();
        }
        BCWorkerThreads.executeWorkTask(new TaskMiniChunkAnalyser(data));
    }

    private void fill() {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = data.min.add(x, y, z);
                    byte expense = getExpenseFromWorld(pos);
                    data.expenseArray[x][y][z] = expense;
                    if (expense < 0) {
                        data.hasNonAir = true;
                    }
                }
            }
        }
        BCLog.logger.info(data.min + " [filler] Has Non Air: " + data.hasNonAir);
    }

    private byte getExpenseFromWorld(BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(world, pos)) {
            return EXPENSE_AIR;
        }
        Material material = block.getMaterial();
        if (material.isLiquid()) {
            return EXPENSE_FLUID;
        }
        if (!material.isSolid()) {
            return EXPENSE_AIR;
        }
        return -1;
    }
}
