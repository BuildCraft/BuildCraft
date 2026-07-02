/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.robotics.zone;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MaterialColor;

public class ZonePlannerMapChunk {
    private final MapColourData[][] data = new MapColourData[16][16];

    public ZonePlannerMapChunk(Level world, ZonePlannerMapChunkKey key) {
        LevelChunk chunk = world.getChunk(key.chunkPos.x, key.chunkPos.z);
        int maxY = world.getMaxBuildHeight() - 1;
        int minY = world.getMinBuildHeight();
        int baseX = key.chunkPos.x << 4;
        int baseZ = key.chunkPos.z << 4;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = baseX + x;
                int worldZ = baseZ + z;
                int topY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                if (topY > maxY) {
                    topY = maxY;
                }
                if (topY < minY) {
                    topY = minY;
                }

                for (int y = topY; y >= minY; y--) {
                    pos.set(worldX, y, worldZ);
                    BlockState state = chunk.getBlockState(pos);
                    MaterialColor mapColor = state.getMapColor(world, pos);
                    if (mapColor == null || mapColor == MaterialColor.NONE) {
                        continue;
                    }

                    data[x][z] = new MapColourData(y, toGuiArgb(mapColor.calculateRGBColor(MaterialColor.Brightness.NORMAL)));
                    break;
                }
            }
        }
    }

    public ZonePlannerMapChunk(FriendlyByteBuf buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (buffer.readBoolean()) {
                    int posY = buffer.readInt();
                    int colour = buffer.readInt();
                    data[x][z] = new MapColourData(posY, colour);
                }
            }
        }
    }

    public void write(FriendlyByteBuf buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MapColourData colour = data[x][z];
                buffer.writeBoolean(colour != null);
                if (colour != null) {
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


    /**
     * BC7 Zone Planner did not build its own terrain palette and did not average/tone-map colours.
     * It stored the top visible block's vanilla map colour index and the client expanded that index
     * through MapColor.mapColorArray[index].colorValue. Modern Minecraft exposes the final colour
     * through MaterialColor#calculateRGBColor, so the port keeps the same idea: first non-air map
     * colour from the surface, normal brightness, no biome tint, no smoothing, no custom overrides.
     *
     * MaterialColor#calculateRGBColor returns the packed format vanilla map textures feed to
     * NativeImage#setPixelRGBA, while GuiComponent.fill expects ARGB. Swap red/blue once here and
     * keep the rest of the pipeline as plain ARGB.
     */
    private static int toGuiArgb(int nativeMapColour) {
        int alpha = nativeMapColour & 0xFF_00_00_00;
        int red = (nativeMapColour & 0x00_00_00_FF) << 16;
        int green = nativeMapColour & 0x00_00_FF_00;
        int blue = (nativeMapColour & 0x00_FF_00_00) >> 16;
        return alpha | red | green | blue;
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
