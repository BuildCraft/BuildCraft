package buildcraft.api.transport;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import buildcraft.core.lib.utils.Utils;

/** Use this class to register blocks with custom block sizes so that pipes can connect to them properly. Note that you
 * do not need to register a custom pipe connection if your block implements ICustomPipeConnection. The registered
 * version does not override your own implementation. */
public final class PipeConnectionAPI {
    private static final Map<Block, ICustomPipeConnection> connections = Maps.newHashMap();
    private static final ICustomPipeConnection DEFAULT = new ICustomPipeConnection() {
        @Override
        public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
            Block block = state.getBlock();
            AxisAlignedBB bb = block.getCollisionBoundingBox(world, pos, state);
            if (bb == null) {
                return 0;
            }
            // Invert the face (If it was up, we want to get the bottom of the block)
            if (face.getAxisDirection() == AxisDirection.POSITIVE) {
                Vec3 min = Utils.min(bb).subtract(Utils.convert(pos));
                return (float) Utils.getValue(min, face.getAxis());
            } else {
                Vec3 max = Utils.max(bb).subtract(Utils.convert(pos));
                return 1 - (float) Utils.getValue(max, face.getAxis());
            }
        }
    };

    /** Register a block with a custom connection. Useful if you don't own the block class or are adding it for some-one
     * 
     * @param block The block instance
     * @param connection The connection instance */
    public static void registerConnection(Block block, ICustomPipeConnection connection) {
        connections.put(block, connection);
    }

    /** Gets the current custom connection that the block uses */
    public static ICustomPipeConnection getCustomConnection(Block block) {
        if (block instanceof ICustomPipeConnection) {
            return (ICustomPipeConnection) block;
        }
        ICustomPipeConnection connection = connections.get(block);
        if (connection != null) {
            return connection;
        }
        return DEFAULT;
    }
}
