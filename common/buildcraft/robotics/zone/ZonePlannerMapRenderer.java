/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.zone;

import buildcraft.lib.client.model.MutableVertex;
import buildcraft.robotics.zone.ZonePlannerMapChunk.MapColourData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@OnlyIn(Dist.CLIENT)
public enum ZonePlannerMapRenderer {
    INSTANCE;

    // private static final Cache<ZonePlannerMapChunkKey, Integer> CHUNK_GL_CACHE = CacheBuilder.newBuilder()
    private static final Cache<ZonePlannerMapChunkKey, VertexBuffer> CHUNK_GL_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .removalListener(ZonePlannerMapRenderer::onRemove)
            .build();
    private final MutableVertex vertex = new MutableVertex();

    // private static void onRemove(RemovalNotification<ZonePlannerMapChunkKey, Integer> notification)
    private static void onRemove(RemovalNotification<ZonePlannerMapChunkKey, VertexBuffer> notification) {
//        Integer glList = notification.getValue();
        VertexBuffer vertexBuffer = notification.getValue();
//        if (glList != null)
        if (vertexBuffer != null) {
//            GL11.glDeleteLists(glList, 1);
            vertexBuffer.close();
        }
    }

    private void vertex(VertexConsumer builder, double x, double y, double z) {
        vertex.positiond(x, y, z);
//        vertex.render(builder);
//        vertex.renderPositionColour(poseStack.last(), builder);
        vertex.renderPositionColour(builder);
    }

    // Calen 1.20.1: changed the order of the vertexes
    public void drawBlockCuboid(VertexConsumer builder, double x, double y, double z, double height, double radius) {
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

        vertex.multColourd(0.6);
        vertex.normalf(-1, 0, 0);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x - rX, y + rY, z - rZ);
        vertex(builder, x - rX, y - rY, z - rZ);
        vertex(builder, x - rX, y - rY, z + rZ);

        vertex.normalf(1, 0, 0);
        vertex(builder, x + rX, y - rY, z + rZ);
        vertex(builder, x + rX, y - rY, z - rZ);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x + rX, y + rY, z + rZ);
        vertex.multColourd(1 / 0.6);

        vertex.multColourd(0.8);
        vertex.normalf(0, 0, 1);
        vertex(builder, x - rX, y + rY, z + rZ);
        vertex(builder, x - rX, y - rY, z + rZ);
        vertex(builder, x + rX, y - rY, z + rZ);
        vertex(builder, x + rX, y + rY, z + rZ);

        vertex.normalf(0, 0, -1);
        vertex(builder, x + rX, y + rY, z - rZ);
        vertex(builder, x + rX, y - rY, z - rZ);
        vertex(builder, x - rX, y - rY, z - rZ);
        vertex(builder, x - rX, y + rY, z - rZ);
        vertex.multColourd(1 / 0.8);
    }

    // public void drawBlockCuboid(BufferBuilder builder, double x, double y, double z, double height)
    public void drawBlockCuboid(VertexConsumer builder, double x, double y, double z, double height) {
//        drawBlockCuboid(builder, x, y, z, height, 0.5);
        drawBlockCuboid(builder, x, y, z, height, 0.5);
    }

    // public void drawBlockCuboid(BufferBuilder builder, double x, double y, double z)
    public void drawBlockCuboid(VertexConsumer builder, double x, double y, double z) {
//        drawBlockCuboid(builder, x, y, z, 1);
        drawBlockCuboid(builder, x, y, z, 1);
    }

    // public OptionalInt getChunkGlList(ZonePlannerMapChunkKey key)
    public Optional<VertexBuffer> getChunkGlList(ZonePlannerMapChunkKey key) {
//        Integer glList = CHUNK_GL_CACHE.getIfPresent(key);
        VertexBuffer vertexBuffer = CHUNK_GL_CACHE.getIfPresent(key);
//        if (glList == null)
        if (vertexBuffer == null) {
            genChunk(key);
//            genChunk(key);
//            glList = CHUNK_GL_CACHE.getIfPresent(key);
            vertexBuffer = CHUNK_GL_CACHE.getIfPresent(key);
        }
//        return glList != null
//                ? OptionalInt.of(glList)
//                : OptionalInt.empty();
        return vertexBuffer != null
                ? Optional.of(vertexBuffer)
                : Optional.empty();
    }

    // public void setColor(int color)
    public void setColor(byte r, byte g, byte b, byte a) {
//        vertex.colouri(color >> 16, color >> 8, color, color >> 24);
        vertex.colouri(r, g, b, a);
    }

    public void setMapColorABGR(int mapColorABGR) {
        vertex.colouri(mapColorABGR >> 0, mapColorABGR >> 8, mapColorABGR >> 16, mapColorABGR >> 24);
    }

    private void genChunk(ZonePlannerMapChunkKey key) {
//        ZonePlannerMapChunk zonePlannerMapChunk = ZonePlannerMapDataClient.INSTANCE.getChunk(Minecraft.getMinecraft().world, key);
        ZonePlannerMapChunk zonePlannerMapChunk = ZonePlannerMapDataClient.INSTANCE.getChunk(Minecraft.getInstance().level, key);
        if (zonePlannerMapChunk == null) {
            return;
        }
//        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        VertexBuffer vertexBuffer = new VertexBuffer();
//        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR); // TODO: normals
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL); // TODO: normals
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MapColourData data = zonePlannerMapChunk.getData(x, z);
                if (data != null) {
                    setMapColorABGR(data.colour);
                    drawBlockCuboid(
                            builder,
//                            poseStack,
//                            key.chunkPos.getXStart() + x,
                            key.chunkPos.getMinBlockX() + x,
                            data.posY,
//                            key.chunkPos.getZStart() + z,
                            key.chunkPos.getMinBlockZ() + z,
                            data.posY
                    );
                }
            }
        }
//        int glList = GL11.glGenLists(1);
//        GL11.glNewList(glList, GL11.GL_COMPILE);
//        Tessellator.getInstance().draw();
        builder.end();
//        GL11.glEndList();
        vertexBuffer.upload(builder);
//        CHUNK_GL_CACHE.put(key, glList);
        CHUNK_GL_CACHE.put(key, vertexBuffer);
    }
}
