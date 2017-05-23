/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.InvalidInputDataException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Template extends Snapshot {
    public boolean[][][] data;

    @Override
    public <T extends ITileForSnapshotBuilder> SnapshotBuilder<T> createBuilder(T tile) {
        // noinspection unchecked
        return (SnapshotBuilder<T>) new TemplateBuilder((ITileForTemplateBuilder) tile);
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
        public final List<BlockPos> toBreak = new ArrayList<>();
        public final List<BlockPos> toPlace = new ArrayList<>();

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
