/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/** Marker interface used by {@link BlockBCBase_Neptune} to automatically add an {@link Direction} property to blocks,
 * and go to and from meta. */
public interface IBlockWithFacing extends ICustomRotationHandler {
    default boolean canFaceVertically() {
        return false;
    }

    default Property<Direction> getFacingProperty() {
        return canFaceVertically() ? BlockBCBase_Neptune.BLOCK_FACING_6 : BlockBCBase_Neptune.PROP_FACING;
    }

    default boolean canBeRotated(IWorld world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    default ActionResultType attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (!canBeRotated(world, pos, state)) {
            return ActionResultType.FAIL;
        }
        Direction currentFacing = state.getValue(getFacingProperty());
        Direction newFacing = canFaceVertically() ? RotationUtil.rotateAll(currentFacing) : currentFacing.getClockWise();
        world.setBlock(pos, state.setValue(getFacingProperty(), newFacing), 3);
        return ActionResultType.SUCCESS;
    }
}
