/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): BlockView / WorldType removed in 1.20.1 deferred
package buildcraft.lib.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/** An {@link BlockView} for getting the properties of a single {@link BlockState}
 * at the {@link SingleBlockAccess#POS} */
public class SingleBlockAccess implements BlockView {
    public static final BlockPos POS = BlockPos.ORIGIN;
    public final BlockState state;

    public SingleBlockAccess(BlockState state) {
        this.state = state;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return POS.equals(pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return 256;
    }

    @Override
    public int getBottomY() {
        return 0;
    }
}
