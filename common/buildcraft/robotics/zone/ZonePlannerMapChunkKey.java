/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class ZonePlannerMapChunkKey {
    public static final int LEVEL_HEIGHT = 32;

    public final ChunkPos chunkPos;
    // public final int dimensionalId;
    public final ResourceKey<Level> dimensionalId;
    public final int level;
    private final int hash;

    // public ZonePlannerMapChunkKey(ChunkPos chunkPos, int dimensionalId, int level)
    public ZonePlannerMapChunkKey(ChunkPos chunkPos, ResourceKey<Level> dimensionalId, int level) {
        this.chunkPos = chunkPos;
        this.dimensionalId = dimensionalId;
        this.level = level;
//        hash = Arrays.hashCode(new int[] { chunkPos.x, chunkPos.z, dimensionalId, level });
        hash = Arrays.hashCode(new Object[] { chunkPos.x, chunkPos.z, dimensionalId, level });
    }

    // public ZonePlannerMapChunkKey(ByteBuf buf)
    public ZonePlannerMapChunkKey(FriendlyByteBuf buf) {
        chunkPos = new ChunkPos(buf.readInt(), buf.readInt());
        dimensionalId = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        level = buf.readInt();
//        hash = Arrays.hashCode(new int[] { chunkPos.x, chunkPos.z, dimensionalId, level });
        hash = Arrays.hashCode(new Object[] { chunkPos.x, chunkPos.z, dimensionalId, level });
    }

    // public void toBytes(ByteBuf buf)
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(chunkPos.x);
        buf.writeInt(chunkPos.z);
//        buf.writeInt(dimensionalId);
        buf.writeResourceLocation(dimensionalId.location());
        buf.writeInt(level);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;
        ZonePlannerMapChunkKey other = (ZonePlannerMapChunkKey) o;
//        if (dimensionalId != other.dimensionalId) return false;
        if (!dimensionalId.equals(other.dimensionalId)) return false;
        if (level != other.level) return false;
        return chunkPos.x == other.chunkPos.x && chunkPos.z == other.chunkPos.z;

    }

    @Override
    public int hashCode() {
        return hash;
    }
}
