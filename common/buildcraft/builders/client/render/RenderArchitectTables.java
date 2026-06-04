/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point3f;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.DetachedRenderer;

import buildcraft.builders.BCBuildersConfig;
import buildcraft.builders.client.ClientArchitectTables;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.render.VertexFormat;

@Environment(EnvType.CLIENT)
public enum RenderArchitectTables implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;

    // @Override -- removed: method does not exist in Fabric 1.20.1
    public void render(PlayerEntity player, float partialTicks) {
        List<Box> boxes = new ArrayList<>(ClientArchitectTables.BOXES.keySet());
        boxes.sort(
            Comparator.<Box>comparingDouble(bb ->
                bb.getCenter().distanceTo(player.getPos())
            ).reversed()
        );
        List<BlockPos> poses = new ArrayList<>(ClientArchitectTables.SCANNED_BLOCKS.keySet());
        poses.sort(
            Comparator.<BlockPos>comparingDouble(pos ->
                new Vec3d(pos.getX(), pos.getY(), pos.getZ()).distanceTo(player.getPos())
            ).reversed()
        );

        final boolean __STENCIL = BCBuildersConfig.enableStencil && MinecraftClient.getInstance().getFramebuffer().isStencilEnabled();

        for (Box bb : boxes) {
            if (__STENCIL) {
            GL11.glStencilMask(0xff);
            GL11.glClearStencil(1);
            GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);
            GL11.glEnable(GL11.GL_STENCIL_TEST);

            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_ZERO, GL11.GL_ZERO, GL11.GL_REPLACE);
            GL11.glStencilMask(0xFF);
            GL11.glDepthMask(false);
            GL11.glColorMask(false, false, false, false);
            }
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();
            if (__STENCIL) {
            buffer.begin(VertexFormat.DrawMode.QUADS, DefaultVertexFormats.POSITION);
            bb = bb.expand(0.01);
            buffer.vertex(bb.minX, bb.maxY, bb.minZ).next();
            buffer.vertex(bb.maxX, bb.maxY, bb.minZ).next();
            buffer.vertex(bb.maxX, bb.minY, bb.minZ).next();
            buffer.vertex(bb.minX, bb.minY, bb.minZ).next();
            buffer.vertex(bb.minX, bb.minY, bb.maxZ).next();
            buffer.vertex(bb.maxX, bb.minY, bb.maxZ).next();
            buffer.vertex(bb.maxX, bb.maxY, bb.maxZ).next();
            buffer.vertex(bb.minX, bb.maxY, bb.maxZ).next();
            buffer.vertex(bb.minX, bb.minY, bb.minZ).next();
            buffer.vertex(bb.maxX, bb.minY, bb.minZ).next();
            buffer.vertex(bb.maxX, bb.minY, bb.maxZ).next();
            buffer.vertex(bb.minX, bb.minY, bb.maxZ).next();
            buffer.vertex(bb.minX, bb.maxY, bb.maxZ).next();
            buffer.vertex(bb.maxX, bb.maxY, bb.maxZ).next();
            buffer.vertex(bb.maxX, bb.maxY, bb.minZ).next();
            buffer.vertex(bb.minX, bb.maxY, bb.minZ).next();
            buffer.vertex(bb.minX, bb.minY, bb.maxZ).next();
            buffer.vertex(bb.minX, bb.maxY, bb.maxZ).next();
            buffer.vertex(bb.minX, bb.maxY, bb.minZ).next();
            buffer.vertex(bb.minX, bb.minY, bb.minZ).next();
            buffer.vertex(bb.maxX, bb.minY, bb.minZ).next();
            buffer.vertex(bb.maxX, bb.maxY, bb.minZ).next();
            buffer.vertex(bb.maxX, bb.maxY, bb.maxZ).next();
            buffer.vertex(bb.maxX, bb.minY, bb.maxZ).next();
            Tessellator.getInstance().draw();
            GL11.glStencilMask(0x00);
            GL11.glDepthMask(true);
            GL11.glColorMask(true, true, true, true);
            }
            RenderSystem.disableDepthTest();
            if (__STENCIL) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            }
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, 
                new Identifier(
                    "buildcraftbuilders",
                    "textures/blocks/scan.png"
                )
            );
            buffer.begin(VertexFormat.DrawMode.QUADS, DefaultVertexFormats.BLOCK);
            for (BlockPos pos : poses) {
                if (!bb.intersects(new Box(pos))) {
                    continue;
                }
                for (Direction face : Direction.values()) {
                    ModelUtil.createFace(
                        face,
                        new Point3f(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F),
                        new Point3f(0.5F, 0.5F, 0.5F),
                        new ModelUtil.UvFaceData(0, 0, 1, 1)
                    )
                        .lighti(15, 15)
                        .colouri(
                            255,
                            255,
                            255,
                            ClientArchitectTables.SCANNED_BLOCKS.get(pos)
                                * 50
                                / ClientArchitectTables.START_SCANNED_BLOCK_VALUE
                        )
                        .render(buffer);
                }
            }
            Tessellator.getInstance().draw();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            if (__STENCIL) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
        }
    }

    @Override
    public void render(float partialTicks) { /* STUB */ }
}
