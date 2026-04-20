/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.lib.misc.HashUtil;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.lib.misc.RotationUtil;
import ct.buildcraft.lib.misc.StringUtilBC;
import ct.buildcraft.lib.misc.VecUtil;
import ct.buildcraft.lib.misc.data.Box;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

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

    
    public static int posToIndex(int sizeX, int sizeY, int sizeZ, int x, int y, int z) {
        return ((z * sizeY) + y) * sizeX + x;
    }

    public static int posToIndex(BlockPos size, int x, int y, int z) {
        return posToIndex(size.getX(), size.getY(), size.getZ(), x, y, z);
    }

    
    public static int posToIndex(int sizeX, int sizeY, int sizeZ, BlockPos pos) {
        return posToIndex(sizeX, sizeY, sizeZ, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static int posToIndex(BlockPos size, BlockPos pos) {
        return posToIndex(size.getX(), size.getY(), size.getZ(), pos.getX(), pos.getY(), pos.getZ());
    }

    public int posToIndex(int x, int y, int z) {
        return posToIndex(size, x, y, z);
    }
    
    public int posToIndex(BlockPos pos) {
        return posToIndex(size, pos);
    }

    public static BlockPos indexToPos(int sizeX, int sizeY, int sizeZ, int i) {
        return new BlockPos(
            i % sizeX,
            (i / sizeX) % sizeY,
            i / (sizeY * sizeX)
        );
    }

    public static BlockPos indexToPos(BlockPos size, int i) {
        return indexToPos(size.getX(), size.getY(), size.getZ(), i);
    }

    public BlockPos indexToPos(int i) {
        return indexToPos(size, i);
    }

    public static int getDataSize(int x, int y, int z) {
        return x * y * z;
    }

    public static int getDataSize(BlockPos size) {
        return getDataSize(size.getX(), size.getY(), size.getZ());
    }

    public int getDataSize() {
        return getDataSize(size);
    }

    public static CompoundTag writeToNBT(Snapshot snapshot) {
        CompoundTag nbt = snapshot.serializeNBT();
        nbt.put("type", NBTUtilBC.writeEnum(snapshot.getType()));
        return nbt;
    }

    public static Snapshot readFromNBT(CompoundTag nbt) throws InvalidInputDataException {
        Tag tag = nbt.get("type");
        EnumSnapshotType type = NBTUtilBC.readEnum(tag, EnumSnapshotType.class);
        if (type == null) {
            throw new InvalidInputDataException("Unknown snapshot type " + tag);
        }
        Snapshot snapshot = Snapshot.create(type);
        snapshot.deserializeNBT(nbt);
        return snapshot;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("key", key.serializeNBT());
        nbt.put("size", NbtUtils.writeBlockPos(size));
        nbt.put("facing", NBTUtilBC.writeEnum(facing));
        nbt.put("offset", NbtUtils.writeBlockPos(offset));
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        key = new Key(nbt.getCompound("key"));
        size = NbtUtils.readBlockPos(nbt.getCompound("size"));
        facing = NBTUtilBC.readEnum(nbt.get("facing"), Direction.class);
        offset = NbtUtils.readBlockPos(nbt.getCompound("offset"));
    }

    abstract public Snapshot copy();

    abstract public EnumSnapshotType getType();

    public void computeKey() {
        CompoundTag nbt = writeToNBT(this);
        if (nbt.contains("key", Tag.TAG_COMPOUND)) {
            nbt.remove("key");
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

        public Key() {
            this.hash = new byte[0];
            this.header = null;
        }

        public Key(Key oldKey, byte[] hash) {
            this.hash = hash;
            this.header = oldKey.header;
        }

        public Key(Key oldKey, @Nullable Header header) {
            this.hash = oldKey.hash;
            this.header = header;
        }

        public Key(CompoundTag nbt) {
            hash = nbt.getByteArray("hash");
            header = nbt.contains("header") ? new Header(nbt.getCompound("header")) : null;
        }

        public Key(FriendlyByteBuf buffer) {
            hash = buffer.readByteArray();
            header = buffer.readBoolean() ? new Header(buffer) : null;
        }

        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putByteArray("hash", hash);
            if (header != null) {
                nbt.put("header", header.serializeNBT());
            }
            return nbt;
        }

        public void writeToByteBuf(FriendlyByteBuf buffer) {
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

        public Header(Key key, UUID owner, Date created, String name) {
            this.key = key;
            this.owner = owner;
            this.created = created;
            this.name = name;
        }

        public Header(CompoundTag nbt) {
            key = new Key(nbt.getCompound("key"));
            owner = nbt.getUUID("owner");
            created = new Date(nbt.getLong("created"));
            name = nbt.getString("name");
        }

        public Header(FriendlyByteBuf buffer) {
            key = new Key(buffer);
            owner = buffer.readUUID();
            created = new Date(buffer.readLong());
            name = buffer.readUtf();
        }

        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.put("key", key.serializeNBT());
            nbt.putUUID("owner", owner);
            nbt.putLong("created", created.getTime());
            nbt.putString("name", name);
            return nbt;
        }

        public void writeToByteBuf(FriendlyByteBuf buffer) {
            key.writeToByteBuf(buffer);
            buffer.writeUUID(owner);
            buffer.writeLong(created.getTime());
            buffer.writeUtf(name);
        }

        public Player getOwnerPlayer(Level world) {
            return world.getPlayerByUUID(owner);
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

    public abstract class BuildingInfo {
        public final BlockPos basePos;
        public final BlockPos offsetPos;
        public final Rotation rotation;
        public final Box box = new Box();

        protected BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.offsetPos = basePos.offset(offset.rotate(rotation));
            this.rotation = rotation;
            this.box.extendToEncompass(toWorld(BlockPos.ZERO));
            this.box.extendToEncompass(toWorld(size.subtract(VecUtil.POS_ONE)));
        }

        public BlockPos toWorld(BlockPos blockPos) {
            return blockPos
                .rotate(rotation)
                .offset(offsetPos);
        }

        public BlockPos fromWorld(BlockPos blockPos) {
            return blockPos
                .subtract(offsetPos)
                .rotate(RotationUtil.invert(rotation));
        }

        public abstract Snapshot getSnapshot();
    }
}
