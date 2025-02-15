/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import buildcraft.api.core.IZone;
import buildcraft.lib.misc.NBTUtilBC;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.util.*;

public class ZonePlan implements IZone {
    private final HashMap<ChunkPos, ZoneChunk> chunkMapping = new HashMap<>();

    public ZonePlan() {
    }

    public ZonePlan(ZonePlan old) {
        for (ChunkPos chunkPos : old.chunkMapping.keySet()) {
            chunkMapping.put(chunkPos, new ZoneChunk(old.chunkMapping.get(chunkPos)));
        }
    }

    public boolean get(int x, int z) {
        int xChunk = x >> 4;
        int zChunk = z >> 4;
        ChunkPos chunkId = new ChunkPos(xChunk, zChunk);
        ZoneChunk property;

        if (!chunkMapping.containsKey(chunkId)) {
            return false;
        } else {
            property = chunkMapping.get(chunkId);
            return property.get(x & 0xF, z & 0xF);
        }
    }

    public void set(int x, int z, boolean val) {
        int xChunk = x >> 4;
        int zChunk = z >> 4;
        ChunkPos chunkId = new ChunkPos(xChunk, zChunk);
        ZoneChunk property;

        if (!chunkMapping.containsKey(chunkId)) {
            if (val) {
                property = new ZoneChunk();
                chunkMapping.put(chunkId, property);
            } else {
                return;
            }
        } else {
            property = chunkMapping.get(chunkId);
        }

        property.set(x & 0xF, z & 0xF, val);

        if (property.isEmpty()) {
            chunkMapping.remove(chunkId);
        }
    }

    public List<Vector2i> getAll() {
        ImmutableList.Builder<Vector2i> builder = ImmutableList.builder();
        for (int zChunk = 0; zChunk < 16; zChunk++) {
            for (int xChunk = 0; xChunk < 16; xChunk++) {
                if (get(xChunk, zChunk)) {
                    builder.add(new Vector2i(xChunk, zChunk));
                }
            }
        }
        chunkMapping.forEach((chunkPos, zoneChunk) ->
        {
            List<Vector2i> zoneChunkAll = zoneChunk.getAll();
            zoneChunkAll.forEach(p -> p.add(new Vector2i(chunkPos.getMaxBlockX(), chunkPos.getMinBlockZ())));
            builder.addAll(zoneChunkAll);
        });
        return builder.build();
    }

    public ZonePlan getWithOffset(int offsetX, int offsetY) {
        ZonePlan zonePlan = new ZonePlan();
        getAll().forEach(p -> zonePlan.set(p.x + offsetX, p.y + offsetY, true));
        return zonePlan;
    }

    public boolean hasChunk(ChunkPos chunkPos) {
        return chunkMapping.containsKey(chunkPos);
    }

    public Set<ChunkPos> getChunkPoses() {
        return chunkMapping.keySet();
    }

    public HashMap<ChunkPos, ZoneChunk> getChunkMapping() {
        return chunkMapping;
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.put(
                "chunkMapping",
                NBTUtilBC.writeCompoundList(
                        chunkMapping.entrySet().stream()
                                .map(entry ->
                                {
                                    CompoundTag zoneChunkTag = new CompoundTag();
                                    entry.getValue().writeToNBT(zoneChunkTag);
                                    zoneChunkTag.putInt("chunkX", entry.getKey().x);
                                    zoneChunkTag.putInt("chunkZ", entry.getKey().z);
                                    return zoneChunkTag;
                                })
                )
        );
    }

    public void readFromNBT(CompoundTag nbt) {
        NBTUtilBC.readCompoundList(nbt.get("chunkMapping"))
                .forEach(zoneChunkTag ->
                {
                    ZoneChunk chunk = new ZoneChunk();
                    chunk.readFromNBT(zoneChunkTag);
                    chunkMapping.put(
                            new ChunkPos(
                                    zoneChunkTag.getInt("chunkX"),
                                    zoneChunkTag.getInt("chunkZ")
                            ),
                            chunk
                    );
                });
    }

    @Override
    public double distanceTo(BlockPos index) {
        return Math.sqrt(distanceToSquared(index));
    }

    @Override
    public double distanceToSquared(BlockPos index) {
        double maxSqrDistance = Double.MAX_VALUE;

        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            double dx = (e.getKey().x << 4 + 8) - index.getX();
            double dz = (e.getKey().z << 4 + 8) - index.getZ();

            double sqrDistance = dx * dx + dz * dz;

            if (sqrDistance < maxSqrDistance) {
                maxSqrDistance = sqrDistance;
            }
        }

        return maxSqrDistance;
    }

    @Override
    public boolean contains(Vec3 point) {
        int xBlock = (int) Math.floor(point.x);
        int zBlock = (int) Math.floor(point.z);

        return get(xBlock, zBlock);
    }

    @Override
    public BlockPos getRandomBlockPos(Random rand) {
        if (chunkMapping.size() == 0) {
            return null;
        }

        int chunkId = rand.nextInt(chunkMapping.size());

        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            if (chunkId == 0) {
                BlockPos i = e.getValue().getRandomBlockPos(rand);
                int x = (e.getKey().x << 4) + i.getX();
                int z = (e.getKey().z << 4) + i.getZ();

                return new BlockPos(x, i.getY(), z);
            }

            chunkId--;
        }

        return null;
    }

    public ZonePlan readFromByteBuf(FriendlyByteBuf buf) {
        chunkMapping.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            ChunkPos key = new ChunkPos(buf.readInt(), buf.readInt());
            ZoneChunk value = new ZoneChunk();
            value.readFromByteBuf(buf);
            chunkMapping.put(key, value);
        }
        return this;
    }

    public void writeToByteBuf(FriendlyByteBuf buf) {
        buf.writeInt(chunkMapping.size());
        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            buf.writeInt(e.getKey().x);
            buf.writeInt(e.getKey().z);
            e.getValue().writeToByteBuf(buf);
        }
    }
}
