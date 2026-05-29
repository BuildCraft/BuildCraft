/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.block;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;

import buildcraft.lib.misc.RotationUtil;

/** Marker interface used by {@link BlockBCBase_Neptune} to automatically add a {@link Direction} property to blocks. */
public interface IBlockWithFacing extends ICustomRotationHandler {
    default boolean canFaceVertically() {
        return false;
    }

    default Property<Direction> getFacingProperty() {
        return canFaceVertically() ? BlockBCBase_Neptune.BLOCK_FACING_6 : BlockBCBase_Neptune.PROP_FACING;
    }

    default boolean canBeRotated(World world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    default ActionResult attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (!canBeRotated(world, pos, state)) {
            return ActionResult.FAIL;
        }
        Direction currentFacing = state.get(getFacingProperty());
        Direction newFacing = canFaceVertically() ? RotationUtil.rotateAll(currentFacing) : currentFacing.rotateYClockwise();
        world.setBlockState(pos, state.with(getFacingProperty(), newFacing));
        return ActionResult.SUCCESS;
    }
}
