/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.misc.data.Box;

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

    @SuppressWarnings("WeakerAccess")
    public class BuildingInfo {
        public final BlockPos basePos;
        public final Rotation rotation;
        public final Box box;
        public final Set<BlockPos> toBreak = new HashSet<>();
        public final Set<BlockPos> toPlace = new HashSet<>();

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.rotation = rotation;
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        BlockPos blockPos = new BlockPos(x, y, z).rotate(rotation)
                            .add(basePos)
                            .add(offset.rotate(rotation));
                        if (!data.get(posToIndex(x, y, z))) {
                            toBreak.add(blockPos);
                        } else {
                            toPlace.add(blockPos);
                        }
                    }
                }
            }
            box = new Box();
            Stream.concat(toBreak.stream(), toPlace.stream()).forEach(box::extendToEncompass);
        }

        public Template getSnapshot() {
            return Template.this;
        }

        public Box getBox() {
            return box;
        }
    }
}
