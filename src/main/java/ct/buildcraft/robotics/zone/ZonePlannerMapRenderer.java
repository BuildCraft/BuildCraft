/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.robotics.zone;

import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import ct.buildcraft.lib.client.model.MutableVertex;
import ct.buildcraft.robotics.zone.ZonePlannerMapChunk.MapColourData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ZonePlannerMapRenderer {
    INSTANCE;

    private static final Cache<ZonePlannerMapChunkKey, Integer> CHUNK_GL_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .removalListener(ZonePlannerMapRenderer::onRemove)
            .build();
    private final MutableVertex vertex = new MutableVertex();

    private static void onRemove(RemovalNotification<ZonePlannerMapChunkKey, Integer> notification) {
        Integer glList = notification.getValue();
        if (glList != null) {
            GL11.glDeleteLists(glList, 1);
        }
    }

    private void vertex(BufferBuilder builder, double x, double y, double z) {
        vertex.positiond(x, y, z);
       // vertex.renderTex(builder);//TODO
    }

    public void drawBlockCuboid(BufferBuilder builder, double x, double y, double z, double height, double radius) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        double rX = radius;
        double rY = height * 0.5;
        @SuppressWarnings("UnnecessaryLocalVariable")
        double rZ = radius;

        y -= rY;

        vertex.normalf(0, 1, 0);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x + rX, y + rY, z + rZ);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x - rX, y + rY, z - rZ);

        vertex.normalf(-1, 0, 0);
        vertex.multColourd(0.6);
        vertex(builder, x - rX, y - rY, z + rZ);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x - rX, y + rY, z - rZ);
        vertex(builder, x - rX, y - rY, z - rZ);

        vertex.normalf(1, 0, 0);
        vertex(builder, x + rX, y - rY, z - rZ);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x + rX, y + rY, z + rZ);
        vertex(builder, x + rX, y - rY, z + rZ);
        vertex.multColourd(1 / 0.6);

        vertex.normalf(0, 0, -1);
        vertex.multColourd(0.8);
        vertex(builder, x - rX, y - rY, z - rZ);
        vertex(builder, x - rX, y + rY, z - rZ);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x + rX, y - rY, z - rZ);

        vertex.normalf(0, 0, 1);
        vertex(builder, x + rX, y - rY, z + rZ);
        vertex(builder, x + rX, y + rY, z + rZ);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x - rX, y - rY, z + rZ);
        vertex.multColourd(1 / 0.8);
    }

    public void drawBlockCuboid(BufferBuilder builder, double x, double y, double z, double height) {
        drawBlockCuboid(builder, x, y, z, height, 0.5);
    }

    public void drawBlockCuboid(BufferBuilder builder, double x, double y, double z) {
        drawBlockCuboid(builder, x, y, z, 1);
    }

    public OptionalInt getChunkGlList(ZonePlannerMapChunkKey key) {
        Integer glList = CHUNK_GL_CACHE.getIfPresent(key);
        if (glList == null) {
            genChunk(key);
            glList = CHUNK_GL_CACHE.getIfPresent(key);
        }
        return glList != null
                ? OptionalInt.of(glList)
                : OptionalInt.empty();
    }

    public void setColor(int color) {
        vertex.colouri(color >> 16, color >> 8, color, color >> 24);
    }

    private void genChunk(ZonePlannerMapChunkKey key) {
        ZonePlannerMapChunk zonePlannerMapChunk = ZonePlannerMapDataClient.INSTANCE.getChunk(Minecraft.getInstance().level, key);
        if (zonePlannerMapChunk == null) {
            return;
        }
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR); // TODO: normals
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MapColourData data = zonePlannerMapChunk.getData(x, z);
                if (data != null) {
                    setColor(data.colour);
                    drawBlockCuboid(
                            builder,
                            key.chunkPos.getMinBlockX() + x,
                            data.posY,
                            key.chunkPos.getMinBlockZ() + z,
                            data.posY
                    );
                }
            }
        }
//        int glList = GL11.glGenLists(1);
//        GL11.glNewList(glList, GL11.GL_COMPILE);//TODO
        Tesselator.getInstance().end();
//        GL11.glEndList();
//        CHUNK_GL_CACHE.put(key, glList);
    }
}
