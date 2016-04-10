package buildcraft.transport;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.transport.ICustomPipeConnection;
import buildcraft.core.lib.utils.Utils;

public enum DefaultPipeConnection implements ICustomPipeConnection {
    INSTANCE;

    @Override
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
        Block block = state.getBlock();
        AxisAlignedBB bb = block.getCollisionBoundingBox(world, pos, state);
        if (bb == null) {
            return 0;
        }
        // Invert the face (If it was up, we want to get the bottom of the block)
        if (face.getAxisDirection() == AxisDirection.POSITIVE) {
            Vec3d min = Utils.min(bb).subtract(Utils.convert(pos));
            return (float) Utils.getValue(min, face.getAxis());
        } else {
            Vec3d max = Utils.max(bb).subtract(Utils.convert(pos));
            return 1 - (float) Utils.getValue(max, face.getAxis());
        }
    }
}
