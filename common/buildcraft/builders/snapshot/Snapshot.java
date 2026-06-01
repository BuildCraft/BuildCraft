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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.Direction;
import net.minecraft.util.BlockRotation;
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
    public Direction facing;
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

    public static NbtCompound writeToNBT(Snapshot snapshot) {
        NbtCompound nbt = snapshot.createNbt();
        nbt.put("type", NBTUtilBC.writeEnum(snapshot.getType()));
        return nbt;
    }

    public static Snapshot readFromNBT(NbtCompound nbt) throws InvalidInputDataException {
        NbtElement tag = nbt.get("type");
        EnumSnapshotType type = NBTUtilBC.readEnum(tag, EnumSnapshotType.class);
        if (type == null) {
            throw new InvalidInputDataException("Unknown snapshot type " + tag);
        }
        Snapshot snapshot = Snapshot.create(type);
        snapshot.deserializeNBT(nbt);
        return snapshot;
    }

    public NbtCompound createNbt() { return serializeNBT(); }
    public NbtCompound serializeNBT() {
        NbtCompound nbt = new NbtCompound();
        nbt.put("key", key.createNbt());
        nbt.put("size", net.minecraft.nbt.NbtHelper.fromBlockPos(size));
        nbt.put("facing", NBTUtilBC.writeEnum(facing));
        nbt.put("offset", net.minecraft.nbt.NbtHelper.fromBlockPos(offset));
        return nbt;
    }

    public void deserializeNBT(NbtCompound nbt) throws InvalidInputDataException {
        key = new Key(nbt.getCompound("key"));
        size = net.minecraft.nbt.NbtHelper.toBlockPos(nbt.getCompound("size"));
        facing = NBTUtilBC.readEnum(nbt.get("facing"), Direction.class);
        offset = net.minecraft.nbt.NbtHelper.toBlockPos(nbt.getCompound("offset"));
    }

    abstract public Snapshot copy();

    abstract public EnumSnapshotType getType();

    public void computeKey() {
        NbtCompound nbt = writeToNBT(this);
        if (nbt.contains("key", NbtElement.COMPOUND_TYPE)) {
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
        public Key(NbtCompound nbt) {
            hash = nbt.getByteArray("hash");
            header = nbt.contains("header") ? new Header(nbt.getCompound("header")) : null;
        }

        public Key(PacketBufferBC buffer) {
            hash = buffer.readByteArray();
            header = buffer.readBoolean() ? new Header(buffer) : null;
        }

        public NbtCompound serializeNBT() {
            NbtCompound nbt = new NbtCompound();
            nbt.putByteArray("hash", hash);
            if (header != null) {
                nbt.put("header", header.createNbt());
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
        public Header(NbtCompound nbt) {
            key = new Key(nbt.getCompound("key"));
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

        public NbtCompound serializeNBT() {
            NbtCompound nbt = new NbtCompound();
            nbt.put("key", key.createNbt());
            nbt.setUniqueId("owner", owner);
            nbt.putLong("created", created.getTime());
            nbt.putString("name", name);
            return nbt;
        }

        public void writeToByteBuf(PacketBufferBC buffer) {
            key.writeToByteBuf(buffer);
            buffer.writeUniqueId(owner);
            buffer.writeLong(created.getTime());
            buffer.writeString(name);
        }

        public PlayerEntity getOwnerPlayer(World world) {
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
        public final net.minecraft.util.BlockRotation rotation;
        public final Box box = new Box();

        protected BuildingInfo(BlockPos basePos, net.minecraft.util.BlockRotation rotation) {
            this.basePos = basePos;
            this.offsetPos = basePos.add(offset.rotate(rotation));
            this.rotation = rotation;
            this.box.extendToEncompass(toWorld(BlockPos.ORIGIN));
            this.box.extendToEncompass(toWorld(size.subtract(VecUtil.POS_ONE)));
        }

        public BlockPos toWorld(BlockPos blockPos) {
            return blockPos
                .rotate(rotation)
                .add(offsetPos);
        }

        public BlockPos fromWorld(BlockPos blockPos) {
            return blockPos
                .subtract(offsetPos)
                .rotate(RotationUtil.invert(rotation));
        }

        public abstract Snapshot getSnapshot();
    }
}
