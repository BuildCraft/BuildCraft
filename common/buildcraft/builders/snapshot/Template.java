/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

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
    public boolean[][][] data;

    public Template clone() {
        Template template = new Template();
        template.size = size;
        template.facing = facing;
        template.offset = offset;
        template.data = new boolean[size.getX()][size.getY()][size.getZ()];
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    template.data[x][y][z] = data[x][y][z];
                }
            }
        }
        template.computeKey();
        return template;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        byte[] serializedData = new byte[size.getX() * size.getY() * size.getZ()];
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    serializedData[i++] = data[x][y][z] ? (byte) 1 : (byte) 0;
                }
            }
        }
        nbt.setByteArray("data", serializedData);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        super.deserializeNBT(nbt);
        data = new boolean[size.getX()][size.getY()][size.getZ()];
        byte[] serializedData = nbt.getByteArray("data");
        if (serializedData.length != size.getX() * size.getY() * size.getZ()) {
            throw new InvalidInputDataException(
                "Serialized data has length of " + serializedData.length +
                    ", but we expected " + size.getX() * size.getY() * size.getZ() + size.toString()
            );
        }
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    data[x][y][z] = serializedData[i++] != 0;
                }
            }
        }
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.TEMPLATE;
    }

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
                        if (!data[x][y][z]) {
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
