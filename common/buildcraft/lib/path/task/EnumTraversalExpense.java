package buildcraft.lib.path.task;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum EnumTraversalExpense {
    AIR((byte) 1),
    FLUID((byte) 3),
    SOLID((byte) 63);

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
        if (mat.blocksMovement()) {
            return SOLID;
        }
        return AIR;
    }
}
