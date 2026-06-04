/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): SchematicBlockFluid deferred
package buildcraft.builders.snapshot;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;

public class SchematicBlockFluid implements ISchematicBlock {

    public static boolean predicate(SchematicBlockContext context) {
        return false;
    }

    @Override
    public void init(SchematicBlockContext context) {}

    @Override
    public ISchematicBlock getRotated(BlockRotation rotation) { return this; }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) { return false; }

    @Override
    public boolean build(World world, BlockPos blockPos) { return false; }

    @Override
    public boolean buildWithoutChecks(World world, BlockPos blockPos) { return false; }

    @Override
    public boolean isBuilt(World world, BlockPos blockPos) { return false; }

    @Override
    public NbtCompound serializeNBT() { return new NbtCompound(); }

    @Override
    public void deserializeNBT(NbtCompound nbt) throws InvalidInputDataException {}
}
