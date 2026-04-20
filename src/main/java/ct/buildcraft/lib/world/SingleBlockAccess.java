/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/** An {@link BlockGetter} for getting the properties of a single {@link BlockState}
 * at the {@link SingleBlockAccess#POS} */
public class SingleBlockAccess implements BlockGetter {
    public static final BlockPos POS = BlockPos.ZERO;
    public final BlockState state;

    public SingleBlockAccess(BlockState state) {
        this.state = state;
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return POS.equals(pos) ? state : Blocks.AIR.defaultBlockState();
    }

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return POS.equals(pos) ? state.getFluidState() : Fluids.EMPTY.defaultFluidState();
	}


	@Override
	public int getHeight() {
		return POS.getY();
	}

	@Override
	public int getMinBuildHeight() {
		return 512;
	}

}
