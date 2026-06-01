/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.Direction;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.fluid.Fluid;
import buildcraft.lib.compat.FluidStackBC;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

import buildcraft.lib.misc.BlockUtil;

public class SchematicBlockFluid implements ISchematicBlock {
    private BlockState blockState;
    private boolean isFlowing;

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return BlockUtil.getFluidWithFlowing(context.getWorld(), context.pos) != null &&
            (BlockUtil.getFluid(context.getWorld(), context.pos) == null ||
                BlockUtil.getFluidWithoutFlowing(context.getWorld().getBlockState(context.pos)) != null);
    }

    @Override
    public void init(SchematicBlockContext context) {
        blockState = context.blockState;
        isFlowing = BlockUtil.getFluid(context.getWorld(), context.pos) == null;
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return Stream.concat(Arrays.stream(Direction.HORIZONTALS), Stream.of(Direction.DOWN))
            .map(Direction::getDirectionVec)
            .map(BlockPos::new)
            .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public List<FluidStackBC> computeRequiredFluids() {
        return Optional.ofNullable(BlockUtil.getFluidWithoutFlowing(blockState))
            .map(fluid -> new FluidStackBC(fluid, Fluid.BUCKET_VOLUME))
            .map(Collections::singletonList)
            .orElseGet(Collections::emptyList);
    }

    @Override
    public SchematicBlockFluid getRotated(net.minecraft.util.BlockRotation rotation) {
        SchematicBlockFluid schematicBlock = SchematicBlockManager.createCleanCopy(this);
        schematicBlock.blockState = blockState;
        schematicBlock.isFlowing = isFlowing;
        return schematicBlock;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return world.isAir(blockPos) ||
            BlockUtil.getFluidWithFlowing(world, blockPos) == BlockUtil.getFluidWithFlowing(blockState.getBlock()) &&
                BlockUtil.getFluid(world, blockPos) == null;
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        if (isFlowing) {
            return true;
        }
        if (world.setBlockState(blockPos, blockState, 11)) {
            Stream.concat(
                Stream.of(Direction.values())
                    .map(Direction::getDirectionVec)
                    .map(BlockPos::new),
                Stream.of(BlockPos.ORIGIN)
            )
                .map(blockPos::add)
                .forEach(updatePos -> world.notifyNeighborsOfStateChange(updatePos, blockState.getBlock(), false));
            return true;
        }
        return false;
    }

    @Override
    public boolean buildWithoutChecks(World world, BlockPos blockPos) {
        return world.setBlockState(blockPos, blockState, 0);
    }

    @Override
    public boolean isBuilt(World world, BlockPos blockPos) {
        return isFlowing || BlockUtil.blockStatesEqual(blockState, world.getBlockState(blockPos));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public NbtCompound createNbt() { return serializeNBT(); }
    public NbtCompound serializeNBT() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("blockState", net.minecraft.nbt.NbtHelper.fromBlockState(blockState));
        nbt.putBoolean("isFlowing", isFlowing);
        return nbt;
    }

    @Override
    public void deserializeNBT(NbtCompound nbt) throws InvalidInputDataException {
        blockState = net.minecraft.nbt.NbtHelper.toBlockState(nbt.getCompound("blockState"));
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
