/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.render.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.client.model.MutableVertex;
import buildcraft.lib.client.sprite.DynamicTextureBC;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.RenderUtil.AutoTessellator;
import buildcraft.lib.misc.data.WorldPos;

import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlannerMapChunk;
import buildcraft.robotics.zone.ZonePlannerMapChunkKey;
import buildcraft.robotics.zone.ZonePlannerMapDataClient;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import buildcraft.lib.misc.GlStateManagerCompat;
import net.minecraft.client.render.VertexFormat;

public class RenderZonePlanner extends TileEntitySpecialRenderer<TileZonePlanner> {
    private static final Cache<WorldPos, DynamicTextureBC> TEXTURES = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES).removalListener(RenderZonePlanner::onRemove).build();
    private static final int TEXTURE_WIDTH = 10;
    private static final int TEXTURE_HEIGHT = 8;

    private static void onRemove(RemovalNotification<WorldPos, DynamicTextureBC> notification) {
        DynamicTextureBC texture = notification.getValue();
        if (texture != null) {
            texture.deleteGlTexture();
        }
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public final void render(TileZonePlanner tile, double x, double y, double z, float partialTicks, int destroyStage,
        float alpha) {
        MinecraftClient.getInstance().getProfiler().push("bc");
        MinecraftClient.getInstance().getProfiler().push("zone");

        double offset = 0.001;
        double minX = 3 / 16D - offset;
        double maxX = 13 / 16D + offset;
        double minY = 5 / 16D - offset;
        double maxY = 13 / 16D + offset;
        double minZ = -offset;
        double maxZ = 1 + offset;

        BlockState state = tile.getWorld().getBlockState(tile.getPos());
        if (state.getBlock() != BCRoboticsBlocks.zonePlanner) {
            return;
        }
        Direction side = state.get(BuildCraftProperties.BLOCK_FACING).getOpposite();

        DynamicTextureBC texture = getTexture(tile, side);
        if (texture == null) {
            return;
        }
        try (AutoTessellator tessellator = RenderUtil.getThreadLocalUnusedTessellator()) {
            BufferBuilder buffer = tessellator.tessellator.getBuffer();
            texture.updateTexture();
            texture.bindGlTexture();
            GlStateManagerCompat.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            ;
            GlStateManagerCompat.setActiveTexture(OpenGlHelper.defaultTexUnit);
            RenderSystem.disableBlend();
            GlStateManager.disableCull();
            if (MinecraftClient.isAmbientOcclusionEnabled()) {
                GlStateManagerCompat.shadeModel(GL11.GL_SMOOTH);
            } else {
                GlStateManagerCompat.shadeModel(GL11.GL_FLAT);
            }

            buffer.begin(VertexFormat.DrawMode.QUADS, DefaultVertexFormats.BLOCK);
            // TODO(R.Chen): setTranslation removed — use MatrixStack instead: buffer.setTranslation(x, y, z);

            Vec3d min;
            Vec3d max;

            float minU = 0;
            float maxU = texture.getMaxU();
            float minV = 0;
            float maxV = texture.getMaxV();

            switch (side) {
                case NORTH:
                    min = new Vec3d(minX, minY, maxZ);
                    max = new Vec3d(maxX, maxY, maxZ);
                    break;
                case EAST:
                    min = new Vec3d(minZ, minY, minX);
                    max = new Vec3d(minZ, maxY, maxX);
                    break;
                case SOUTH:
                    min = new Vec3d(minX, minY, minZ);
                    max = new Vec3d(maxX, maxY, minZ);
                    break;
                case WEST:
                default:
                    min = new Vec3d(maxZ, minY, minX);
                    max = new Vec3d(maxZ, maxY, maxX);
                    break;
            }

            MutableVertex vertex = new MutableVertex();

            vertex.colouri(-1);
            vertex.lighti(0xF, 0xF);

            vertex.positiond(min.x, min.y, min.z).texf(minU, minV).render(buffer);
            vertex.positiond(max.x, min.y, max.z).texf(maxU, minV).render(buffer);
            vertex.positiond(max.x, max.y, max.z).texf(maxU, maxV).render(buffer);
            vertex.positiond(min.x, max.y, min.z).texf(minU, maxV).render(buffer);

            // TODO(R.Chen): setTranslation removed — use MatrixStack instead: buffer.setTranslation(0, 0, 0);
            tessellator.tessellator.draw();
        }
        RenderHelper.enableStandardItemLighting();

        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    private static DynamicTextureBC getTexture(TileZonePlanner tile, Direction side) {
        if (TEXTURES.getIfPresent(new WorldPos(tile)) == null) {
            DynamicTextureBC texture = createTexture(tile, side);
            if (texture != null) {
                TEXTURES.put(new WorldPos(tile), texture);
            }
        }
        return TEXTURES.getIfPresent(new WorldPos(tile));
    }

    private static DynamicTextureBC createTexture(TileZonePlanner tile, Direction side) {
        DynamicTextureBC texture = new DynamicTextureBC(TEXTURE_WIDTH, TEXTURE_HEIGHT);
        for (int textureX = 0; textureX < TEXTURE_WIDTH; textureX++) {
            for (int textureY = 0; textureY < TEXTURE_HEIGHT; textureY++) {
                int posX;
                int posZ;
                int scale = 4;
                int offset1 = (textureX - TEXTURE_WIDTH / 2) * scale;
                int offset2 = (textureY - TEXTURE_HEIGHT / 2) * scale;
                switch (side) {
                    case NORTH:
                        posX = tile.getPos().getX() + offset1;
                        posZ = tile.getPos().getZ() - offset2;
                        break;
                    case EAST:
                        posX = tile.getPos().getX() + offset2;
                        posZ = tile.getPos().getZ() + offset1;
                        break;
                    case SOUTH:
                        posX = tile.getPos().getX() + offset1;
                        posZ = tile.getPos().getZ() + offset2;
                        break;
                    case WEST:
                    default:
                        posX = tile.getPos().getX() - offset2;
                        posZ = tile.getPos().getZ() + offset1;
                        break;
                }
                ChunkPos chunkPos = new ChunkPos(posX >> 4, posZ >> 4);
                texture.setColor(textureX, textureY, -1);
                ZonePlannerMapChunkKey key =
                    new ZonePlannerMapChunkKey(chunkPos, tile.getWorld().provider.getDimension(), tile.getLevel());
                ZonePlannerMapChunk zonePlannerMapChunk =
                    ZonePlannerMapDataClient.INSTANCE.getChunk(tile.getWorld(), key);
                if (zonePlannerMapChunk != null) {
                    texture.setColor(textureX, textureY, zonePlannerMapChunk.getColour(posX, posZ) | 0xFF_00_00_00);
                } else {
                    return null;
                }
            }
        }
        return texture;
    }

    @Override
    public void render(TileZonePlanner entity, float tickDelta, net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light, int overlay) { /* STUB */ }
}
