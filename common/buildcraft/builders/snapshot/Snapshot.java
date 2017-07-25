/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;

import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.net.PacketBufferBC;

public abstract class Snapshot {
    public Key key = new Key(new byte[0]);
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

    abstract public EnumSnapshotType getType();

    public void computeKey() {
        NBTTagCompound nbt = writeToNBT(this);
        if (nbt.hasKey("key", Constants.NBT.TAG_COMPOUND)) {
            nbt.removeTag("key");
        }
        key = new Key(HashUtil.computeHash(nbt));
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

        @SuppressWarnings("WeakerAccess")
        public Key(byte[] hash) {
            this.hash = hash;
        }

        @SuppressWarnings("WeakerAccess")
        public Key(NBTTagCompound nbt) {
            hash = nbt.getByteArray("hash");
        }

        @SuppressWarnings("WeakerAccess")
        public Key(PacketBufferBC buffer) {
            hash = buffer.readByteArray();
        }

        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setByteArray("hash", hash);
            return nbt;
        }

        public void writeToByteBuf(PacketBufferBC buffer) {
            buffer.writeByteArray(hash);
        }

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && Arrays.equals(hash, ((Key) o).hash);
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

        public Header(NBTTagCompound nbt) {
            key = new Key(nbt.getCompoundTag("key"));
            owner = nbt.getUniqueId("owner");
            created = new Date(nbt.getLong("created"));
            name = nbt.getString("name");
        }

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
    }
}
