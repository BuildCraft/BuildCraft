/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render;

import buildcraft.builders.BCBuildersConfig;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.client.ClientArchitectTables;
import buildcraft.lib.client.model.ModelUtil;
import buildcraft.lib.client.render.DetachedRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public enum RenderArchitectTables implements DetachedRenderer.IDetachedRenderer {
    INSTANCE;

    @Override
//    public void render(Player player, float partialTicks)
    public void render(Player player, float partialTicks, PoseStack poseStack) {
        List<AABB> boxes = new ArrayList<>(ClientArchitectTables.BOXES.keySet());
        boxes.sort(
                Comparator.<AABB>comparingDouble(bb ->
//                        bb.getCenter().distanceTo(player.getPositionVector())
                                bb.getCenter().distanceTo(player.position())
                ).reversed()
        );
        List<BlockPos> poses = new ArrayList<>(ClientArchitectTables.SCANNED_BLOCKS.keySet());
        poses.sort(
                Comparator.<BlockPos>comparingDouble(pos ->
//                        new Vec3(pos).distanceTo(player.getPositionVector())
                                Vec3.atLowerCornerOf(pos).distanceTo(player.position())
                ).reversed()
        );

//        final boolean __STENCIL = BCBuildersConfig.enableStencil && Minecraft.getMinecraft().getFramebuffer().isStencilEnabled();
        final boolean __STENCIL = BCBuildersConfig.enableStencil;

        for (AABB bb : boxes) {
            // Calen
            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(Sheets.translucentCullBlockSheet());
            // TODO Calen what does this do? seems nothing in 1.12.2
//            if (__STENCIL)
//            {
            // Calen: these will destroy the mc rendering, only dark background with stars left
////                GL11.glStencilMask(0xff);
//                RenderSystem.stencilMask(0xff);
////                GL11.glClearStencil(1);
//                RenderSystem.clearStencil(1);
////                GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);
//                RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
//                GL11.glEnable(GL11.GL_STENCIL_TEST);
//
//                GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
//                GL11.glStencilOp(GL11.GL_ZERO, GL11.GL_ZERO, GL11.GL_REPLACE);
//                GL11.glStencilMask(0xFF);
//                GL11.glDepthMask(false);
//                GL11.glColorMask(false, false, false, false);
//            }
//            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
//            if (__STENCIL)
//            if (false)
//            {
//                // Calen moved here
////                BufferBuilder buffer = Tesselator.getInstance().getBuilder();
//////                buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
////                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
//
//                float u0 = white.get().getU0();
//                float u1 = white.get().getU1();
//                float v0 = white.get().getV0();
//                float v1 = white.get().getV1();
//
//                poseStack.pushPose();
////                bb = bb.grow(0.01);
//                bb = bb.inflate(0.01);
//                this.buffer = buffer;
//                this.pose = poseStack.last();
//                this.vertex(bb.minX, bb.maxY, bb.minZ, u0, v1).endVertex();
//                this.vertex(bb.maxX, bb.maxY, bb.minZ, u1, v1).endVertex();
//                this.vertex(bb.maxX, bb.minY, bb.minZ, u1, v0).endVertex();
//                this.vertex(bb.minX, bb.minY, bb.minZ, u0, v0).endVertex();
//
//                this.vertex(bb.minX, bb.minY, bb.maxZ, u0, v1).endVertex();
//                this.vertex(bb.maxX, bb.minY, bb.maxZ, u1, v1).endVertex();
//                this.vertex(bb.maxX, bb.maxY, bb.maxZ, u1, v0).endVertex();
//                this.vertex(bb.minX, bb.maxY, bb.maxZ, u0, v0).endVertex();
//
//                this.vertex(bb.minX, bb.minY, bb.minZ, u0, v1).endVertex();
//                this.vertex(bb.maxX, bb.minY, bb.minZ, u1, v1).endVertex();
//                this.vertex(bb.maxX, bb.minY, bb.maxZ, u1, v0).endVertex();
//                this.vertex(bb.minX, bb.minY, bb.maxZ, u0, v0).endVertex();
//
//                this.vertex(bb.minX, bb.maxY, bb.maxZ, u0, v1).endVertex();
//                this.vertex(bb.maxX, bb.maxY, bb.maxZ, u1, v1).endVertex();
//                this.vertex(bb.maxX, bb.maxY, bb.minZ, u1, v0).endVertex();
//                this.vertex(bb.minX, bb.maxY, bb.minZ, u0, v0).endVertex();
//
//                this.vertex(bb.minX, bb.minY, bb.maxZ, u0, v1).endVertex();
//                this.vertex(bb.minX, bb.maxY, bb.maxZ, u1, v1).endVertex();
//                this.vertex(bb.minX, bb.maxY, bb.minZ, u1, v0).endVertex();
//                this.vertex(bb.minX, bb.minY, bb.minZ, u0, v0).endVertex();
//
//                this.vertex(bb.maxX, bb.minY, bb.minZ, u0, v1).endVertex();
//                this.vertex(bb.maxX, bb.maxY, bb.minZ, u1, v1).endVertex();
//                this.vertex(bb.maxX, bb.maxY, bb.maxZ, u1, v0).endVertex();
//                this.vertex(bb.maxX, bb.minY, bb.maxZ, u0, v0).endVertex();
//                this.buffer = null;
//                this.pose = null;
//////                Tessellator.getInstance().draw();
////                Tesselator.getInstance().end();
//                poseStack.popPose();
////                GL11.glStencilMask(0x00);
//                RenderSystem.stencilMask(0x00);
////                GL11.glDepthMask(true);
//                RenderSystem.depthMask(true);
////                GL11.glColorMask(true, true, true, true);
//                RenderSystem.colorMask(true, true, true, true);
//            }
////            GlStateManager.disableDepth();
//            RenderSystem.disableDepthTest();
//            if (__STENCIL)
//            {
////                GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
//                RenderSystem.stencilFunc(GL11.GL_EQUAL, 1, 0xFF);
//            }
////            GlStateManager.enableBlend();
//            RenderSystem.enableBlend();
////            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            Minecraft.getMinecraft().renderEngine.bindTexture(
//                    new ResourceLocation(
//                            "buildcraftbuilders",
//                            "textures/blocks/scan.png"
//                    )
//            );
//            RenderSystem.setShaderTexture(
//                    0, new ResourceLocation(
//                            "buildcraftbuilders",
//                            "textures/blocks/scan.png"
//                    ));
            TextureAtlasSprite scan = BCBuildersSprites.ARCHITECT_SCAN.getSprite();
            float u0 = scan.getU0(), u1 = scan.getU1(), v0 = scan.getV0(), v1 = scan.getV1();
//            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            for (BlockPos pos : poses) {
                if (!bb.intersects(new AABB(pos))) {
                    continue;
                }
                poseStack.pushPose();
//                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                for (Direction face : Direction.VALUES) {
                    ModelUtil.createFace(
                                    face,
                                    new Vector3f(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F),
//                                    new Point3f(0.5F, 0.5F, 0.5F),
                                    new Vector3f(0.5F, 0.5F, 0.5F),
//                                    new ModelUtil.UvFaceData(0, 0, 1, 1)
                                    new ModelUtil.UvFaceData(u0, v0, u1, v1)
                            )
                            .lighti((byte) 15, (byte) 15)
                            .colouri(
                                    255,
                                    255,
                                    255,
                                    ClientArchitectTables.SCANNED_BLOCKS.get(pos)
                                            * 50
                                            / ClientArchitectTables.START_SCANNED_BLOCK_VALUE
                            )
                            .render(poseStack.last(), buffer);
                }
                poseStack.popPose();
            }
//            Tessellator.getInstance().draw();
////            GlStateManager.disableBlend();
//            RenderSystem.disableBlend();
////            GlStateManager.enableDepth();
//            RenderSystem.enableDepthTest();
//            if (__STENCIL)
//            {
////                GL11.glDisable(GL11.GL_STENCIL_TEST);
//                RenderSystem.clearStencil(GL11.GL_EQUAL);
//            }
        }
    }

    //    private final LazyLoadedValue<TextureAtlasSprite> white = new LazyLoadedValue<>(SpriteUtil::white);
    private final int COLOUR = (15 << 20) | (15 << 4);
    private VertexConsumer buffer = null;
    private PoseStack.Pose pose = null;

    private VertexConsumer vertex(double x, double y, double z, float u, float v) {
        buffer
//                .vertex(pose.pose(), (float) (x - bb.minX), (float) (y - bb.minY), (float) (z - bb.minZ))
                .vertex(pose.pose(), (float) (x), (float) (y), (float) (z))
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(COLOUR)
                .normal(pose.normal(), 1, 1, 1)
//                .endVertex()
        ;
        return buffer;
    }
}
