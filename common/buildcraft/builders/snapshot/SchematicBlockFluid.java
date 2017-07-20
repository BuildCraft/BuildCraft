/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.lib.misc.BlockUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchematicBlockFluid implements ISchematicBlock<SchematicBlockFluid> {
    private IBlockState blockState;
    private boolean isFlowing;

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return BlockUtil.getFluidWithFlowing(context.world, context.pos) != null;
    }

    @Override
    public void init(SchematicBlockContext context) {
        blockState = context.blockState;
        isFlowing = BlockUtil.getFluid(context.world, context.pos) == null;
    }

    @Override
    public boolean isAir() {
        return false;
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return Stream.concat(Arrays.stream(EnumFacing.HORIZONTALS), Stream.of(EnumFacing.DOWN))
            .map(EnumFacing::getDirectionVec)
            .map(BlockPos::new)
            .collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems(SchematicBlockContext context) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids(SchematicBlockContext context) {
        List<FluidStack> requiredFluids = new ArrayList<>();
        FluidStack fluid = BlockUtil.drainBlock(context.world, context.pos, false);
        if (fluid != null) {
            requiredFluids.add(fluid);
        }
        return requiredFluids;
    }

    @Override
    public SchematicBlockFluid getRotated(Rotation rotation) {
        SchematicBlockFluid schematicBlock = new SchematicBlockFluid();
        schematicBlock.blockState = blockState;
        schematicBlock.isFlowing = isFlowing;
        return schematicBlock;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return world.isAirBlock(blockPos) ||
            BlockUtil.getFluidWithFlowing(world, blockPos) != BlockUtil.getFluidWithFlowing(blockState.getBlock()) &&
                BlockUtil.getFluid(world, blockPos) == null;
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        if (isFlowing) {
            return true;
        }
        if (world.setBlockState(blockPos, blockState, 11)) {
            Stream.concat(
                Stream.of(EnumFacing.VALUES)
                    .map(EnumFacing::getDirectionVec)
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

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("blockState", NBTUtil.writeBlockState(new NBTTagCompound(), blockState));
        nbt.setBoolean("isFlowing", isFlowing);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        blockState = NBTUtil.readBlockState(nbt.getCompoundTag("blockState"));
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
