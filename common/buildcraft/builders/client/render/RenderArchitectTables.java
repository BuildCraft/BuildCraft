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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.DetachedRenderer;

import buildcraft.builders.BCBuildersConfig;
import buildcraft.builders.client.ClientArchitectTables;

@SideOnly(Side.CLIENT)
public enum RenderArchitectTables implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;

    @Override
    public void render(EntityPlayer player, float partialTicks) {
        List<AxisAlignedBB> boxes = new ArrayList<>(ClientArchitectTables.BOXES.keySet());
        boxes.sort(
            Comparator.<AxisAlignedBB>comparingDouble(bb ->
                bb.getCenter().distanceTo(player.getPositionVector())
            ).reversed()
        );
        List<BlockPos> poses = new ArrayList<>(ClientArchitectTables.SCANNED_BLOCKS.keySet());
        poses.sort(
            Comparator.<BlockPos>comparingDouble(pos ->
                new Vec3d(pos).distanceTo(player.getPositionVector())
            ).reversed()
        );

        final boolean __STENCIL = BCBuildersConfig.enableStencil && Minecraft.getMinecraft().getFramebuffer().isStencilEnabled();

        for (AxisAlignedBB bb : boxes) {
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
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            bb = bb.grow(0.01);
            buffer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            buffer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            buffer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            buffer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            buffer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            Tessellator.getInstance().draw();
            GL11.glStencilMask(0x00);
            GL11.glDepthMask(true);
            GL11.glColorMask(true, true, true, true);
            }
            GlStateManager.disableDepth();
            if (__STENCIL) {
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
            }
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            Minecraft.getMinecraft().renderEngine.bindTexture(
                new ResourceLocation(
                    "buildcraftbuilders",
                    "textures/blocks/scan.png"
                )
            );
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            for (BlockPos pos : poses) {
                if (!bb.intersects(new AxisAlignedBB(pos))) {
                    continue;
                }
                for (EnumFacing face : EnumFacing.VALUES) {
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
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            if (__STENCIL) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            }
        }
    }
}
