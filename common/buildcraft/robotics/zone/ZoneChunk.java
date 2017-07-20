/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import javax.vecmath.Point2i;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class ZoneChunk {
    public BitSet property;
    private boolean fullSet = false;

    public ZoneChunk() {}

    public ZoneChunk(ZoneChunk old) {
        if (old.property != null) {
            property = BitSet.valueOf(old.property.toLongArray());
        }
    }

    public boolean get(int xChunk, int zChunk) {
        return fullSet || property != null && property.get(xChunk + zChunk * 16);
    }

    public void set(int xChunk, int zChunk, boolean value) {
        if (value) {
            if (fullSet) {
                return;
            }

            if (property == null) {
                property = new BitSet(16 * 16);
            }

            property.set(xChunk + zChunk * 16, true);

            if (property.cardinality() >= 16 * 16) {
                property = null;
                fullSet = true;
            }
        } else {
            if (fullSet) {
                property = new BitSet(16 * 16);
                property.flip(0, 16 * 16 - 1);
                fullSet = false;
            } else if (property == null) {
                // Note - ZonePlan should usually destroy such chunks
                property = new BitSet(16 * 16);
            }

            property.set(xChunk + zChunk * 16, false);
        }
    }

    public List<Point2i> getAll() {
        ImmutableList.Builder<Point2i> builder = ImmutableList.builder();
        for (int zChunk = 0; zChunk < 16; zChunk++) {
            for (int xChunk = 0; xChunk < 16; xChunk++) {
                if (get(xChunk, zChunk)) {
                    builder.add(new Point2i(xChunk, zChunk));
                }
            }
        }
        return builder.build();
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("fullSet", fullSet);

        if (property != null) {
            nbt.setByteArray("bits", property.toByteArray());
        }
    }

    public void readFromNBT(NBTTagCompound nbt) {
        fullSet = nbt.getBoolean("fullSet");

        if (nbt.hasKey("bits")) {
            property = BitSet.valueOf(nbt.getByteArray("bits"));
        }
    }

    public BlockPos getRandomBlockPos(Random rand) {
        int x, z;

        if (fullSet) {
            x = rand.nextInt(16);
            z = rand.nextInt(16);
        } else {
            int bitId = rand.nextInt(property.cardinality());
            int bitPosition = property.nextSetBit(0);

            while (bitId > 0) {
                bitId--;

                bitPosition = property.nextSetBit(bitPosition + 1);
            }

            z = bitPosition / 16;
            x = bitPosition - 16 * z;
        }
        int y = rand.nextInt(255);

        return new BlockPos(x, y, z);
    }

    public boolean isEmpty() {
        return !fullSet && property.isEmpty();
    }

    public ZoneChunk readFromByteBuf(PacketBuffer buf) {
        int flags = buf.readUnsignedByte();
        if ((flags & 1) != 0) {
            property = BitSet.valueOf(buf.readByteArray());
        }
        fullSet = (flags & 2) != 0;

        return this;
    }

    public void writeToByteBuf(PacketBuffer buf) {
        int flags = (fullSet ? 2 : 0) | (property != null ? 1 : 0);
        buf.writeByte(flags);
        if (property != null) {
            buf.writeByteArray(property.toByteArray());
        }
    }
}
