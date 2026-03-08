/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

/** An {@link IWorld} for getting the properties of a single {@link BlockState}
 * at the {@link SingleBlockAccess#POS} */
//public class SingleBlockAccess implements IWorld
public class SingleBlockAccess implements IBlockReader {
    public static final BlockPos POS = BlockPos.ZERO;
    public final BlockState state;

    public SingleBlockAccess(BlockState state) {
        this.state = state;
    }

    @Override
    public TileEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
//    public int getCombinedLight(BlockPos pos, int lightValue)
    public int getLightEmission(BlockPos pos) {
//        return lightValue << 4;
        return 0;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return POS.equals(pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public FluidState getFluidState(BlockPos p_45569_) {
        return null;
    }

//    @Override
//    public boolean isEmptyBlock(BlockPos pos) {
////        return getBlockState(pos).getBlock().isAir(state, this, pos);
//        return getBlockState(pos).isAir();
//    }

//    @Override
////    public Biome getBiome(BlockPos pos) {
//    public Holder<Biome> getBiome(BlockPos pos) {
////        this.getBiomeManager().getBiome(pos);
//        return ForgeRegistries.BIOMES.getHolder(Biomes.PLAINS).get();
//    }

//    @Override
//    public int getStrongPower(BlockPos pos, Direction direction)
//    public int getDirectSignal(BlockPos pos, Direction direction) {
//        return 0;
//    }

//    @Override
//    public RealmsServer.WorldType getWorldType() {
//        return RealmsServer.WorldType.DEBUG_ALL_BLOCK_STATES;
//    }

//    @Override
//    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default)
//    public boolean isFaceSturdy(BlockPos pos, Direction side, boolean _default) {
//        if (POS.equals(pos)) {
//            return _default;
//        }
//        return state.isFaceSturdy(this, pos, side);
//    }

    // since 1.18.2
//    @Override
//    public int getMinBuildHeight() {
//        return -64;
//    }
}
