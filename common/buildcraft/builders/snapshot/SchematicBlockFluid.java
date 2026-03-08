/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.IFakeWorld;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchematicBlockFluid implements ISchematicBlock {
    private BlockState blockState;
    private boolean isFlowing;

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return BlockUtil.getFluidWithFlowing(context.world, context.pos) != null &&
                (BlockUtil.getFluid(context.world, context.pos) == null ||
                        BlockUtil.getFluidWithoutFlowing(context.world.getBlockState(context.pos)) != null);
    }

    @Override
    public void init(SchematicBlockContext context) {
        blockState = context.blockState;
        isFlowing = BlockUtil.getFluid(context.world, context.pos) == null;
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
//        return Stream.concat(Arrays.stream(Direction.HORIZONTALS), Stream.of(Direction.DOWN))
        return Stream.concat(Arrays.stream(Direction.BY_2D_DATA), Stream.of(Direction.DOWN))
//                .map(Direction::getDirectionVec)
                .map(Direction::getNormal)
                .map(BlockPos::new)
                .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        return Optional.ofNullable(BlockUtil.getFluidWithoutFlowing(blockState))
//                .map(fluid -> new FluidStack(fluid, Fluid.BUCKET_VOLUME))
                .map(fluid -> new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME))
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList);
    }

    @Override
    public SchematicBlockFluid getRotated(Rotation rotation) {
        SchematicBlockFluid schematicBlock = SchematicBlockManager.createCleanCopy(this);
        schematicBlock.blockState = blockState;
        schematicBlock.isFlowing = isFlowing;
        return schematicBlock;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return world.isEmptyBlock(blockPos) ||
                BlockUtil.getFluidWithFlowing(world, blockPos) == BlockUtil.getFluidWithFlowing(blockState.getBlock()) &&
                        BlockUtil.getFluid(world, blockPos) == null;
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        if (isFlowing) {
            return true;
        }
        if (world.setBlock(blockPos, blockState, BlockConstants.UPDATE_ALL_IMMEDIATE)) {
            Stream.concat(
                            Stream.of(Direction.values())
//                                    .map(Direction::getDirectionVec)
                                    .map(Direction::getNormal)
                                    .map(BlockPos::new),
//                            Stream.of(BlockPos.ORIGIN)
                            Stream.of(BlockPos.ZERO)
                    )
//                    .map(blockPos::add)
                    .map(blockPos::offset)
//                    .forEach(updatePos -> world.notifyNeighborsOfStateChange(updatePos, blockState.getBlock(), false));
                    .forEach(updatePos -> world.updateNeighborsAt(updatePos, blockState.getBlock()));
            return true;
        }
        return false;
    }

    @Override
//    public boolean buildWithoutChecks(World world, BlockPos blockPos)
    public boolean buildWithoutChecks(IFakeWorld world, BlockPos blockPos) {
        return world.setBlock(blockPos, blockState, 0);
    }

    @Override
    public boolean isBuilt(World world, BlockPos blockPos) {
        return isFlowing || BlockUtil.blockStatesEqual(blockState, world.getBlockState(blockPos));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
//        nbt.put("blockState", NBTUtil.writeBlockState(new CompoundNBT(), blockState));
        nbt.put("blockState", NBTUtil.writeBlockState(blockState));
        nbt.putBoolean("isFlowing", isFlowing);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) throws InvalidInputDataException {
        blockState = NBTUtil.readBlockState(nbt.getCompound("blockState"));
        isFlowing = nbt.getBoolean("isFlowing");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchematicBlockFluid that = (SchematicBlockFluid) o;

        return isFlowing == that.isFlowing && blockState.equals(that.blockState);
    }

    @Override
    public int hashCode() {
        int result = blockState.hashCode();
        result = 31 * result + (isFlowing ? 1 : 0);
        return result;
    }
}
