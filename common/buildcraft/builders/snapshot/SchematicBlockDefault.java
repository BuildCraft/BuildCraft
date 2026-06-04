/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): schematic block logic deferred — Property/IBlockState API migration pending
package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.compat.FluidStackBC;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

public class SchematicBlockDefault implements ISchematicBlock {

    protected final Set<BlockPos> requiredBlockOffsets = new HashSet<>();
    protected BlockState blockState;
    protected final List<Property<?>> ignoredProperties = new ArrayList<>();
    protected NbtCompound tileNbt;
    protected BlockRotation tileRotation = BlockRotation.NONE;
    protected Block placeBlock;

    public static boolean predicate(SchematicBlockContext context) {
        return false;
    }

    @Override
    public void init(SchematicBlockContext context) {
        // STUB
    }

    @Override
    public ISchematicBlock getRotated(BlockRotation rotation) {
        return this;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean build(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean buildWithoutChecks(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean isBuilt(World world, BlockPos blockPos) {
        return false;
    }

    @Override
    public NbtCompound serializeNBT() {
        return new NbtCompound();
    }

    @Override
    public void deserializeNBT(NbtCompound nbt) throws InvalidInputDataException {
        // STUB
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        return new ArrayList<>();
    }

    @Nonnull
    @Override
    public List<FluidStackBC> computeRequiredFluids() {
        return new ArrayList<>();
    }
}
