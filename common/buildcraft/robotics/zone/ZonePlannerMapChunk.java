/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

public class ZonePlannerMapChunk {
    private final MapColourData[][] data = new MapColourData[16][16];

    public ZonePlannerMapChunk(Level world, ZonePlannerMapChunkKey key) {
//        Chunk chunk = world.getChunkFromChunkCoords(key.chunkPos.x, key.chunkPos.z);
        LevelChunk chunk = world.getChunk(key.chunkPos.x, key.chunkPos.z);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // Scan down from the max height value of a chunk until we find a block
                for (int y = key.level * ZonePlannerMapChunkKey.LEVEL_HEIGHT; y > 0; y--) {
//                    int colour = chunk.getBlockState(x, y, z).getMapColor(world, new BlockPos(x, y, z)).colorValue;
                    int colour = chunk.getBlockState(new BlockPos(x, y, z)).getMapColor(world, new BlockPos(x, y, z)).col;
                    if (colour != 0) {
                        data[x][z] = new MapColourData(y, colour);
                        break;
                    }
                }
            }
        }
    }

    // public ZonePlannerMapChunk(PacketBuffer buffer)
    public ZonePlannerMapChunk(FriendlyByteBuf buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int posY = buffer.readInt();
                if (posY > 0) {
                    int colour = buffer.readInt();
                    data[x][z] = new MapColourData(posY, colour);
                }
            }
        }
    }

    // public void write(PacketBuffer buffer)
    public void write(FriendlyByteBuf buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MapColourData colour = data[x][z];
                if (colour == null) {
                    buffer.writeInt(-1);
                } else {
                    buffer.writeInt(colour.posY);
                    buffer.writeInt(colour.colour);
                }
            }
        }
    }

    public int getColour(int x, int z) {
        MapColourData col = getData(x, z);
        return col == null ? -1 : col.colour;
    }

    @Nullable
    public MapColourData getData(int x, int z) {
        return data[x & 15][z & 15];
    }

    public static final class MapColourData {
        public final int posY;
        public final int colour;

        public MapColourData(int posY, int colour) {
            this.posY = posY;
            this.colour = colour;
        }
    }
}
