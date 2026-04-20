package ct.buildcraft.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public interface ICustomRotationHandler {
    InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched);
}
