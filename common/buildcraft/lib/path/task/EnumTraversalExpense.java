package buildcraft.lib.path.task;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum EnumTraversalExpense {
    AIR((byte) 1),
    FLUID((byte) 3),
    /** If you *must* find a path then you can use this, but this isn't included in any of the graphs. */
    SOLID((byte) -1);

    public final byte expense;

    private EnumTraversalExpense(byte expense) {
        this.expense = expense;
    }

    public static EnumTraversalExpense getFor(World world, BlockPos pos) {
        return getFor(world, pos, world.getBlockState(pos));
    }

    public static EnumTraversalExpense getFor(World world, BlockPos pos, IBlockState state) {
        if (world.isAirBlock(pos)) {
            return AIR;
        }
        Material mat = state.getMaterial();
        if (mat.isLiquid()) {
            return FLUID;
        }
        Block block = state.getBlock();
        if (block.isPassable(world, pos)) {
            return AIR;
        }
        return SOLID;
    }
}
