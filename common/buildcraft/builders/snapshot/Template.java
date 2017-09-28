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

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.filler.FilledTemplate;

public class Template extends Snapshot {
    public FilledTemplate data;

    @Override
    public Template copy() {
        Template template = new Template();
        template.size = size;
        template.facing = facing;
        template.offset = offset;
        template.data = new FilledTemplate(data);
        template.computeKey();
        return template;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("data", data.writeToNbt());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        super.deserializeNBT(nbt);
        if (nbt.hasKey("data", Constants.NBT.TAG_BYTE_ARRAY)) {
            data = new FilledTemplate(offset, offset.add(size).add(-1, -1, -1));
            // Compat for 7.99.7 and below
            byte[] oldData = nbt.getByteArray("data");
            BitSet oldSet = BitSet.valueOf(oldData);
            int i = 0;
            for (int z = 0; z < data.sizeZ; z++) {
                for (int y = 0; y < data.sizeY; y++) {
                    for (int x = 0; x < data.sizeX; x++, i++) {
                        if (oldSet.get(i)) {
                            data.fill(x, y, z);
                        }
                    }
                }
            }
        } else if (nbt.hasKey("data", Constants.NBT.TAG_COMPOUND)) {
            data = new FilledTemplate(nbt.getCompoundTag("data"));
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
