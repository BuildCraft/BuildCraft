/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.lib.misc.RotationUtil;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Marker interface used by {@link BlockBCBase_Neptune} to automatically add an {@link EnumFacing} property to blocks,
 * and go to and from meta. */
public interface IBlockWithFacing extends ICustomRotationHandler {
    default boolean canPlacedVertical() {
        return false;
    }

    default IProperty<EnumFacing> getFacingProperty() {
        return this.canPlacedVertical() ? BlockBCBase_Neptune.BLOCK_FACING_6 : BlockBCBase_Neptune.PROP_FACING;
    }

    default boolean canBeRotated(World world, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    default EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        if (!canBeRotated(world, pos, state)) {
            return EnumActionResult.FAIL;
        }
        EnumFacing currentFacing = state.getValue(getFacingProperty());
        EnumFacing newFacing = canPlacedVertical() ? RotationUtil.rotateAll(currentFacing) : currentFacing.rotateY();
        world.setBlockState(pos, state.withProperty(getFacingProperty(), newFacing));
        return EnumActionResult.SUCCESS;
    }
}
