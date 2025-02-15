/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** Marker interface used by {@link BlockBCBase_Neptune} to automatically add an {@link Direction} property to blocks,
 * and go to and from meta. */
public interface IBlockWithFacing extends ICustomRotationHandler {
    default boolean canFaceVertically() {
        return false;
    }

    default Property<Direction> getFacingProperty() {
        return canFaceVertically() ? BlockBCBase_Neptune.BLOCK_FACING_6 : BlockBCBase_Neptune.PROP_FACING;
    }

    default boolean canBeRotated(LevelAccessor world, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    default InteractionResult attemptRotation(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        if (!canBeRotated(world, pos, state)) {
            return InteractionResult.FAIL;
        }
        Direction currentFacing = state.getValue(getFacingProperty());
        Direction newFacing = canFaceVertically() ? RotationUtil.rotateAll(currentFacing) : currentFacing.getClockWise();
        world.setBlock(pos, state.setValue(getFacingProperty(), newFacing), 3);
        return InteractionResult.SUCCESS;
    }
}
