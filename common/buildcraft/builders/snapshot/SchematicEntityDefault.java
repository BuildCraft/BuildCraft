/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
// STUB(R.Chen): schematic entity logic deferred — entity NBT round-trip API pending
package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.lib.compat.FluidStackBC;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;

public class SchematicEntityDefault implements ISchematicEntity {

    private NbtCompound entityNbt;
    private Vec3d pos;
    private BlockRotation entityRotation = BlockRotation.NONE;

    public static boolean predicate(SchematicEntityContext context) {
        return false;
    }

    @Override
    public void init(SchematicEntityContext context) {
        // STUB
    }

    @Override
    public Vec3d getPos() {
        return pos != null ? pos : Vec3d.ZERO;
    }

    @Override
    public ISchematicEntity getRotated(BlockRotation rotation) {
        return this;
    }

    @Override
    public Entity build(World world, BlockPos basePos) {
        return null;
    }

    @Override
    public Entity buildWithoutChecks(World world, BlockPos basePos) {
        return null;
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
