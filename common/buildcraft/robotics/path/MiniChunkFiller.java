package buildcraft.robotics.path;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.robotics.path.MiniChunkCalculationData.CalculationStep;

public class MiniChunkFiller implements Runnable {
    public static final int EXPENSE_AIR = 1;
    public static final int EXPENSE_FLUID = 3;

    private final World world;
    private final MiniChunkCalculationData data;

    public MiniChunkFiller(World world, MiniChunkCalculationData data) {
        this.world = world;
        this.data = data;
    }

    @Override
    public void run() {
        if (data.step != CalculationStep.REQUESTED) {
            throw new IllegalStateException("Wrong state! Expected REQUESTED but found " + data.step + " [no-sync]");
        }
        synchronized (data) {
            if (data.step != CalculationStep.REQUESTED) {
                throw new IllegalStateException("Wrong state! Expected REQUESTED but found " + data.step + " [synchronized]");
            }
            data.step = CalculationStep.FILLING;
            fill();
            data.step = CalculationStep.FILLED;
        }
        MiniChunkCache.WORKER_THREAD_POOL.execute(new MiniChunkAnalyser(data));
    }

    private void fill() {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = data.min.add(x, y, z);
                    data.blockData[x][y][z] = getExpenseFromWorld(pos);
                }
            }
        }
    }

    private int getExpenseFromWorld(BlockPos pos) {
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
        return Integer.MAX_VALUE;
    }
}
