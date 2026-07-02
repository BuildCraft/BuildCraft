/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.SchematicBlockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

public final class SchematicBlockAir implements ISchematicBlock {
    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return true;
    }

    @Override
    public void init(SchematicBlockContext context) {
    }

    @Override
    public boolean isAir() {
        return true;
    }

    @Override
    public SchematicBlockAir getRotated(Rotation rotation) {
        return SchematicBlockManager.createCleanCopy(this);
    }

    @Override
    public boolean canBuild(Level Level, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean build(Level Level, BlockPos blockPos) {
        return true;
    }

    @Override
    public boolean buildWithoutChecks(Level Level, BlockPos blockPos) {
        return true;
    }

    @Override
    public boolean isBuilt(Level Level, BlockPos blockPos) {
        return true;
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass());
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
