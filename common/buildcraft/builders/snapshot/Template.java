/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.BitSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;

public class Template extends Snapshot {
    public BitSet data;

    @Override
    public Template copy() {
        Template template = new Template();
        template.size = size;
        template.facing = facing;
        template.offset = offset;
        template.data = (BitSet) data.clone();
        template.computeKey();
        return template;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setByteArray("data", data.toByteArray());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        super.deserializeNBT(nbt);
        data = BitSet.valueOf(nbt.getByteArray("data"));
        if (data.length() > size.getX() * size.getY() * size.getZ()) {
            throw new InvalidInputDataException(
                "Serialized data has length of " + data.length() +
                    ", but we expected at most " +
                    size.getX() * size.getY() * size.getZ() + " (" + size.toString() + ")"
            );
        }
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.TEMPLATE;
    }

    public class BuildingInfo extends Snapshot.BuildingInfo {
        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            super(basePos, rotation);
        }

        @Override
        public Template getSnapshot() {
            return Template.this;
        }
    }
}
