/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
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
import buildcraft.lib.nbt.NbtSquisher;
import buildcraft.lib.net.PacketBufferBC;

public abstract class Snapshot {
    private static final String NBT_HEADER = "header";
    public Header header;
    public BlockPos size;
    public EnumFacing facing;
    public BlockPos offset;

    public Snapshot(Header header) {
        this.header = header;
    }

    public Snapshot() {}

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
        if (header != null) {
            nbt.setTag(NBT_HEADER, header.serializeNBT());
        }
        nbt.setTag("size", NBTUtil.createPosTag(size));
        nbt.setTag("facing", NBTUtilBC.writeEnum(facing));
        nbt.setTag("offset", NBTUtil.createPosTag(offset));
        return nbt;
    }

    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        Header h = new Header(nbt.getCompoundTag(NBT_HEADER));
        if (h.hash == null || h.hash.length != HashUtil.DIGEST_LENGTH) {
            h = h.withHash(computeHash(nbt));
        }
        header = h;
        size = NBTUtil.getPosFromTag(nbt.getCompoundTag("size"));
        facing = NBTUtilBC.readEnum(nbt.getTag("facing"), EnumFacing.class);
        offset = NBTUtil.getPosFromTag(nbt.getCompoundTag("offset"));
    }

    abstract public EnumSnapshotType getType();

    public final byte[] computeHash() {
        return computeHash(writeToNBT(this));
    }

    private static byte[] computeHash(NBTTagCompound nbt) {
        NBTTagCompound nbtHeader = null;
        if (nbt.hasKey(NBT_HEADER, Constants.NBT.TAG_COMPOUND)) {
            // Don't let the hash depend on the header
            nbtHeader = nbt.getCompoundTag(NBT_HEADER);
            nbt.removeTag(NBT_HEADER);
        }
        try (DigestOutputStream dos = HashUtil.createDigestStream()) {
            NbtSquisher.squishVanillaUncompressed(nbt, new DataOutputStream(dos));
            return dos.getMessageDigest().digest();
        } catch (IOException io) {
            // Shouldn't happen - the digest stream never throws an exception
            throw new RuntimeException(io);
        } finally {
            // Re-add the header nbt - callers probably expect the header tag to still be there
            if (nbtHeader != null) {
                nbt.setTag(NBT_HEADER, nbtHeader);
            }
        }
    }

    @Override
    public String toString() {
        return getType() + " - " + StringUtilBC.blockPosToShortString(size).replace(',', 'x') + " = " + header;
    }

    public static class Header {
        public final byte[] hash;
        public final UUID owner;
        public final Date created;
        public final String name;

        public Header(byte[] hash, UUID owner, Date created, String name) {
            this.hash = hash;
            this.owner = owner;
            this.created = created;
            this.name = name;
        }

        public Header withHash(byte[] newHash) {
            return new Header(newHash, owner, created, name);
        }

        public Header(NBTTagCompound nbt) {
            hash = nbt.getByteArray("hash");
            owner = nbt.getUniqueId("owner");
            created = new Date(nbt.getLong("created"));
            name = nbt.getString("name");
        }

        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setByteArray("hash", hash);
            nbt.setUniqueId("owner", owner);
            nbt.setLong("created", created.getTime());
            nbt.setString("name", name);
            return nbt;
        }

        public Header(PacketBufferBC buffer) {
            hash = buffer.readByteArray();
            owner = buffer.readUniqueId();
            created = new Date(buffer.readLong());
            name = buffer.readString();
        }

        public void writeToByteBuf(PacketBufferBC buffer) {
            buffer.writeByteArray(hash);
            buffer.writeUniqueId(owner);
            buffer.writeLong(created.getTime());
            buffer.writeString(name);
        }

        public EntityPlayer getOwnerPlayer(World world) {
            return world.getPlayerEntityByUUID(owner);
        }

        @Override
        public String toString() {
            return name + " {" + HashUtil.convertHashToString(hash) + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Header header = (Header) o;

            return Arrays.equals(hash, header.hash);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(hash);
        }
    }
}
