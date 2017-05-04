package buildcraft.lib.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.misc.RotationUtil;

/** Marker interface used by {@link BlockBCBase_Neptune} to automatically add an {@link EnumFacing} property to blocks,
 * and go to and from meta. */
public interface IBlockWithFacing extends ICustomRotationHandler {
    default boolean canPlacedVertical() {
        return false;
    }

    default IProperty<EnumFacing> getFacingProperty() {
        return this.canPlacedVertical() ? BlockBCBase_Neptune.BLOCK_FACING_6 : BlockBCBase_Neptune.PROP_FACING;
    }

    default boolean canBeRotated(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        return true;
    }

    @Override
    default EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (!canBeRotated(world, pos, state, sideWrenched)) {
            return EnumActionResult.FAIL;
        }
        EnumFacing currentFacing = state.getValue(getFacingProperty());
        EnumFacing newFacing = canPlacedVertical() ? RotationUtil.rotateAll(currentFacing) : currentFacing.rotateY();
        world.setBlockState(pos, state.withProperty(getFacingProperty(), newFacing));
        return EnumActionResult.SUCCESS;
    }
}
