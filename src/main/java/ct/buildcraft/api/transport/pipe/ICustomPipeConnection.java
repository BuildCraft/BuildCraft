package ct.buildcraft.api.transport.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ICustomPipeConnection {
    /** @return How long the connecting pipe should extend for, in addition to its normal 4/16f connection. Values less
     *         than or equal to <code>-4 / 16.0f</code> indicate that the pipe will not connect at all, and will render
     *         as it it was not connected. */
    float getExtension(Level world, BlockPos pos, Direction face, BlockState state);
}
