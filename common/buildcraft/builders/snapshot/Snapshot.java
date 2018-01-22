/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.RotationUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.net.PacketBufferBC;

public abstract class Snapshot {
    public Key key = new Key();
    public BlockPos size;
    public EnumFacing facing;
    public BlockPos offset;

    public static Snapshot create(EnumSnapshotType type) {
        switch (type) {
            case TEMPLATE:
                return new Template();
            case BLUEPRINT:
                return new Blueprint();
        }
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(int sizeX, int sizeY, int sizeZ, int x, int y, int z) {
        return ((z * sizeY) + y) * sizeX + x;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(BlockPos size, int x, int y, int z) {
        return posToIndex(size.getX(), size.getY(), size.getZ(), x, y, z);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(int sizeX, int sizeY, int sizeZ, BlockPos pos) {
        return posToIndex(sizeX, sizeY, sizeZ, pos.getX(), pos.getY(), pos.getZ());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int posToIndex(BlockPos size, BlockPos pos) {
        return posToIndex(size.getX(), size.getY(), size.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public int posToIndex(int x, int y, int z) {
        return posToIndex(size, x, y, z);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public int posToIndex(BlockPos pos) {
        return posToIndex(size, pos);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static BlockPos indexToPos(int sizeX, int sizeY, int sizeZ, int i) {
        return new BlockPos(
            i % sizeX,
            (i / sizeX) % sizeY,
            i / (sizeY * sizeX)
        );
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static BlockPos indexToPos(BlockPos size, int i) {
        return indexToPos(size.getX(), size.getY(), size.getZ(), i);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public BlockPos indexToPos(int i) {
        return indexToPos(size, i);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int getDataSize(int x, int y, int z) {
        return x * y * z;
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static int getDataSize(BlockPos size) {
        return getDataSize(size.getX(), size.getY(), size.getZ());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public int getDataSize() {
        return getDataSize(size);
    }

    public static NBTTagCompound writeToNBT(Snapshot snapshot) {
        NBTTagCompound nbt = snapshot.serializeNBT();
        nbt.setTag("type", NBTUtilBC.writeEnum(snapshot.getType()));
        return nbt;
    }

    public static Snapshot readFromNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        NBTBase tag = nbt.getTag("type");
        EnumSnapshotType type = NBTUtilBC.readEnum(tag, EnumSnapshotType.class);
        if (type == null) {
            throw new InvalidInputDataException("Unknown snapshot type " + tag);
        }
        Snapshot snapshot = Snapshot.create(type);
        snapshot.deserializeNBT(nbt);
        return snapshot;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("key", key.serializeNBT());
        nbt.setTag("size", NBTUtil.createPosTag(size));
        nbt.setTag("facing", NBTUtilBC.writeEnum(facing));
        nbt.setTag("offset", NBTUtil.createPosTag(offset));
        return nbt;
    }

    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        key = new Key(nbt.getCompoundTag("key"));
        size = NBTUtil.getPosFromTag(nbt.getCompoundTag("size"));
        facing = NBTUtilBC.readEnum(nbt.getTag("facing"), EnumFacing.class);
        offset = NBTUtil.getPosFromTag(nbt.getCompoundTag("offset"));
    }

    abstract public Snapshot copy();

    abstract public EnumSnapshotType getType();

    public void computeKey() {
        NBTTagCompound nbt = writeToNBT(this);
        if (nbt.hasKey("key", Constants.NBT.TAG_COMPOUND)) {
            nbt.removeTag("key");
        }
        key = new Key(key, HashUtil.computeHash(nbt));
    }

    @Override
    public String toString() {
        return "Snapshot{" +
            "key=" + key +
            ", size=" + StringUtilBC.blockPosAsSizeToString(size) +
            ", facing=" + facing +
            ", offset=" + offset +
            "}";
    }

    public static class Key {
        public final byte[] hash;
        @Nullable // for client storage
        public final Header header;

        @SuppressWarnings("WeakerAccess")
        public Key() {
            this.hash = new byte[0];
            this.header = null;
        }

        @SuppressWarnings("WeakerAccess")
        public Key(Key oldKey, byte[] hash) {
            this.hash = hash;
            this.header = oldKey.header;
        }

        @SuppressWarnings("WeakerAccess")
        public Key(Key oldKey, @Nullable Header header) {
            this.hash = oldKey.hash;
            this.header = header;
        }

        @SuppressWarnings("WeakerAccess")
        public Key(NBTTagCompound nbt) {
            hash = nbt.getByteArray("hash");
            header = nbt.hasKey("header") ? new Header(nbt.getCompoundTag("header")) : null;
        }

        public Key(PacketBufferBC buffer) {
            hash = buffer.readByteArray();
            header = buffer.readBoolean() ? new Header(buffer) : null;
        }

        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setByteArray("hash", hash);
            if (header != null) {
                nbt.setTag("header", header.serializeNBT());
            }
            return nbt;
        }

        public void writeToByteBuf(PacketBufferBC buffer) {
            buffer.writeByteArray(hash);
            buffer.writeBoolean(header != null);
            if (header != null) {
                header.writeToByteBuf(buffer);
            }
        }

        @Override
        public boolean equals(Object o) {
            return this == o ||
                o != null &&
                    getClass() == o.getClass() &&
                    Arrays.equals(hash, ((Key) o).hash) &&
                    (header != null ? header.equals(((Key) o).header) : ((Key) o).header == null);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hash);
        }

        @Override
        public String toString() {
            return HashUtil.convertHashToString(hash);
        }
    }

    public static class Header {
        public final Key key;
        public final UUID owner;
        public final Date created;
        public final String name;

        @SuppressWarnings("WeakerAccess")
        public Header(Key key, UUID owner, Date created, String name) {
            this.key = key;
            this.owner = owner;
            this.created = created;
            this.name = name;
        }

        @SuppressWarnings("WeakerAccess")
        public Header(NBTTagCompound nbt) {
            key = new Key(nbt.getCompoundTag("key"));
            owner = nbt.getUniqueId("owner");
            created = new Date(nbt.getLong("created"));
            name = nbt.getString("name");
        }

        @SuppressWarnings("WeakerAccess")
        public Header(PacketBufferBC buffer) {
            key = new Key(buffer);
            owner = buffer.readUniqueId();
            created = new Date(buffer.readLong());
            name = buffer.readString();
        }

        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setTag("key", key.serializeNBT());
            nbt.setUniqueId("owner", owner);
            nbt.setLong("created", created.getTime());
            nbt.setString("name", name);
            return nbt;
        }

        public void writeToByteBuf(PacketBufferBC buffer) {
            key.writeToByteBuf(buffer);
            buffer.writeUniqueId(owner);
            buffer.writeLong(created.getTime());
            buffer.writeString(name);
        }

        public EntityPlayer getOwnerPlayer(World world) {
            return world.getPlayerEntityByUUID(owner);
        }

        @Override
        public boolean equals(Object o) {
            return this == o ||
                o != null &&
                    getClass() == o.getClass() &&
                    key.equals(((Header) o).key) &&
                    owner.equals(((Header) o).owner) &&
                    created.equals(((Header) o).created) &&
                    name.equals(((Header) o).name);
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + owner.hashCode();
            result = 31 * result + created.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public abstract class BuildingInfo {
        public final BlockPos basePos;
        public final BlockPos offsetPos;
        public final Rotation rotation;
        public final Box box = new Box();

        protected BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.offsetPos = basePos.add(rotate(offset, rotation));
            this.rotation = rotation;
            this.box.extendToEncompass(toWorld(BlockPos.ORIGIN));
            this.box.extendToEncompass(toWorld(size.subtract(VecUtil.POS_ONE)));
        }

        public BlockPos toWorld(BlockPos blockPos) {
            return rotate(blockPos, rotation)
                .add(offsetPos);
        }

        public BlockPos fromWorld(BlockPos blockPos) {
            return rotate(blockPos
                .subtract(offsetPos), RotationUtil.invert(rotation));
        }

        public abstract Snapshot getSnapshot();

        //Copy from 1.11+
        public BlockPos rotate(BlockPos pos, Rotation rotationIn)
        {
            switch (rotationIn)
            {
                case NONE:
                default:
                    return pos;
                case CLOCKWISE_90:
                    return new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
                case CLOCKWISE_180:
                    return new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
                case COUNTERCLOCKWISE_90:
                    return new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
            }
        }
    }
}
