/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.world;

import net.minecraft.block.BlockState;
import net.minecraft.init.Biomes;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/** An {@link BlockView} for getting the properties of a single {@link BlockState}
 * at the {@link SingleBlockAccess#POS} */
public class SingleBlockAccess implements BlockView {
    public static final BlockPos POS = BlockPos.ORIGIN;
    public final BlockState state;

    public SingleBlockAccess(BlockState state) {
        this.state = state;
    }

    @Override
    public BlockEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return lightValue << 4;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return POS.equals(pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos).getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.PLAINS;
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.DEBUG_ALL_BLOCK_STATES;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default) {
        if (POS.equals(pos)) {
            return _default;
        }
        return state.isSideSolid(this, pos, side);
    }
}
