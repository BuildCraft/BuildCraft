/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.lib.net.MessageManager;
import com.google.common.base.Predicates;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO Calen
public enum ClientSnapshots {
    INSTANCE;

    private final List<Snapshot> snapshots = new ArrayList<>();
    private final List<Snapshot.Key> pending = new ArrayList<>();
    private final Map<Snapshot.Key, FakeWorld> worlds = new HashMap<>();
    private final Map<Snapshot.Key, BufferBuilder> buffers = new HashMap<>();

    public Snapshot getSnapshot(Snapshot.Key key) {
        Snapshot found = snapshots.stream().filter(snapshot -> snapshot.key.equals(key)).findFirst().orElse(null);
        if (found == null && !pending.contains(key)) {
            pending.add(key);
            MessageManager.sendToServer(new MessageSnapshotRequest(key));
        }
        return found;
    }

    public void onSnapshotReceived(Snapshot snapshot) {
        pending.remove(snapshot.key);
        snapshots.add(snapshot);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSnapshot(Snapshot.Header header, int offsetX, int offsetY, int sizeX, int sizeY, MatrixStack poseStack) {
        if (header == null) {
            return;
        }
        Snapshot snapshot = getSnapshot(header.key);
        if (snapshot == null) {
            return;
        }
        renderSnapshot(snapshot, offsetX, offsetY, sizeX, sizeY, poseStack);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSnapshot(Snapshot snapshot, int offsetX, int offsetY, int sizeX, int sizeY, MatrixStack poseStack) {
        FakeWorld world = worlds.computeIfAbsent(snapshot.key, key ->
        {
            FakeWorld localWorld = new FakeWorld();
            localWorld.uploadSnapshot(snapshot);
            return localWorld;
        });
//        // Calen test
//        BufferBuilder testBuf = Tesselator.getInstance().getBuilder();
//        testBuf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
//        poseStack.pushPose();
////        poseStack.translate(0, 0, 1000);
////        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
////                Blocks.GRASS_BLOCK.defaultBlockState(),
////                poseStack,
//////                                bufferSource,
////                (renderType) -> testBuf,
////                0,
////                OverlayTexture.NO_OVERLAY
////        );
//        TileEntity teeeee=new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
//        TileEntityRenderer<TileEntity> renderer =  Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(teeeee);
//
//        renderer.render(teeeee, 0, poseStack, (renderType)->testBuf, (15<<20)&(15<<4), OverlayTexture.NO_OVERLAY);
//        poseStack.popPose();
//        Tesselator.getInstance().end();

        float partialTicks = Minecraft.getInstance().getFrameTime();
        BufferBuilder bufferBuilder = buffers.computeIfAbsent(snapshot.key, key ->
//        buffers.computeIfAbsent(snapshot.key, key ->
        {
//            IRenderTypeBuffer bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
//            // Calen
//            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
//            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

            BufferBuilder localBuffer = new BufferBuilder(1024) {
                @Override
//                public void reset()
                public void clear() {
                }
            };
            localBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            poseStack.pushPose();
//            poseStack.translate(
//                    -FakeWorld.BLUEPRINT_OFFSET.getX(),
//                    -FakeWorld.BLUEPRINT_OFFSET.getY(),
//                    -FakeWorld.BLUEPRINT_OFFSET.getZ()
//            );
            poseStack.translate(0, 0, 800);
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).offset(FakeWorld.BLUEPRINT_OFFSET);
//                        localBuffer.setTranslation(
//                                -FakeWorld.BLUEPRINT_OFFSET.getX(),
//                                -FakeWorld.BLUEPRINT_OFFSET.getY(),
//                                -FakeWorld.BLUEPRINT_OFFSET.getZ()
//                        );
//                        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(
//                                world.getBlockState(pos),
//                                pos,
//                                world,
////                                localBuffer
//                        );
                        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                                world.getBlockState(pos),
                                poseStack,
//                                bufferSource,
                                (renderType) -> localBuffer,
                                0,
                                OverlayTexture.NO_OVERLAY
                        );
//                        localBuffer.setTranslation(0, 0, 0);
                    }
                }
            }
//            Tesselator.getInstance().end();// Calen add
            poseStack.popPose();
//            localBuffer.finishDrawing();
            return localBuffer;
//            return null;
        });
//        GlStateManager.pushAttrib();
//        GlStateManager.enableDepth();
        RenderSystem.enableDepthTest();
//        GlStateManager.enableBlend();
        RenderUtil.enableBlend();
//        GlStateManager.pushMatrix();
        poseStack.pushPose();
////        GlStateManager.matrixMode(GL11.GL_PROJECTION);
//        RenderSystem.getShader().PROJECTION_MATRIX.set(poseStack.last().pose());
//        GlStateManager.pushMatrix();
        poseStack.pushPose();
////        GlStateManager.loadIdentity();
//        poseStack.setIdentity();
//        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        MainWindow window = Minecraft.getInstance().getWindow();
        double scaleFactor = window.getGuiScale();
//        int viewportX = offsetX * scaledResolution.getScaleFactor();
        int viewportX = (int) (offsetX * scaleFactor);
//        int viewportY = Minecraft.getMinecraft().displayHeight - (sizeY + offsetY) * scaledResolution.getScaleFactor();
        int viewportY = (int) (window.getHeight() - (sizeY + offsetY) * scaleFactor);
//        int viewportWidth = sizeX * scaledResolution.getScaleFactor();
        int viewportWidth = (int) (sizeX * scaleFactor);
//        int viewportHeight = sizeY * scaledResolution.getScaleFactor();
        int viewportHeight = (int) (sizeY * scaleFactor);

        // Calen debug
////        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        GlStateManager._enableScissorTest();
////        GL11.glScissor(
////                viewportX,
////                viewportY,
////                viewportWidth,
////                viewportHeight
////        );
//        GlStateManager._scissorBox(
//                viewportX,
//                viewportY,
//                viewportWidth,
//                viewportHeight
//        );
////        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
//        GlStateManager._clear(GL11.GL_DEPTH_BUFFER_BIT, true); // TODO Calen pClearError = true???
////        GL11.glDisable(GL11.GL_SCISSOR_TEST);
//        GlStateManager._disableScissorTest();

        // TODO Calen debug
////        GlStateManager.viewport(
//        GlStateManager._viewport(
//                viewportX,
//                viewportY,
//                viewportWidth,
//                viewportHeight
//        );
//        GlStateManager.scale(scaledResolution.getScaleFactor(), scaledResolution.getScaleFactor(), 1);
        poseStack.scale((float) scaleFactor, (float) scaleFactor, 1);
        // TODO Calen ???
//        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 0.1F, 1000.0F);
        poseStack.last().pose().multiply(Matrix4f.perspective(70.0F, (float) sizeX / sizeY, 0.1F, 1000.0F));
        // TODO Calen ???
////        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
////        RenderSystem.getShader().MODEL_VIEW_MATRIX.set(poseStack.last().pose());
//        RenderSystem.applyModelViewMatrix();
////        GlStateManager.loadIdentity();
//        poseStack.setIdentity();
//        GlStateManager.enableRescaleNormal();
//        GlStateManager.pushMatrix();
        poseStack.pushPose();
        int snapshotSize = Math.max(Math.max(snapshot.size.getX(), snapshot.size.getY()), snapshot.size.getY());
//        GlStateManager.translate(0, 0, -snapshotSize * 2F - 3);
        poseStack.translate(0, 0, -snapshotSize * 2F - 3);
        // TODO Calen from 1.12.2 correct?
        // TODO Calen debug
//        GlStateManager.rotate(20, 1, 0, 0);
//        GL11.glRotatef(20, 1, 0, 0);
//        RenderSystem.getInverseViewRotationMatrix().set(1, 0, 20);
        poseStack.mulPose(Vector3f.XP.rotation(20));
//        poseStack.mulPose(Vector3f.ZP.rotation(20));
        // TODO Calen from 1.12.2 correct?
        // TODO Calen debug
//        GlStateManager.rotate((System.currentTimeMillis() % 3600) / 10F, 0, 1, 0);
//        GL11.glRotatef((System.currentTimeMillis() % 3600) / 10F, 0, 1, 0);
//        RenderSystem.getInverseViewRotationMatrix().set(0, 1, (System.currentTimeMillis() % 3600) / 10F);
        poseStack.mulPose(Vector3f.YP.rotation(1));
        poseStack.mulPose(Vector3f.ZP.rotation((System.currentTimeMillis() % 3600) / 10F));
//        GlStateManager.translate(-snapshot.size.getX() / 2F, -snapshot.size.getY() / 2F, -snapshot.size.getZ() / 2F);
        poseStack.translate(-snapshot.size.getX() / 2F, -snapshot.size.getY() / 2F, -snapshot.size.getZ() / 2F);
//        GlStateManager.translate(0, snapshotSize * 0.1F, 0);
        poseStack.translate(0, snapshotSize * 0.1F, 0);
//        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        SpriteUtil.bindTexture(AtlasTexture.LOCATION_BLOCKS);
        // TODO Draw blocks in GUI???
//        new WorldVertexBufferUploader().draw(bufferBuilder);
        // Calen: setupRenderState+end will make the main tooltip box background disappear
//        RenderType.translucent().setupRenderState();
//        RenderType.translucent().end(bufferBuilder, 0, 0, 0);
//        RenderType.solid().setupRenderState();
//        RenderType.solid().end(bufferBuilder, 0, 0, 0);
//        RenderType.cutout().setupRenderState();
//        RenderType.cutout().end(bufferBuilder, 0, 0, 0);
//        RenderType.cutoutMipped().setupRenderState();
//        RenderType.cutoutMipped().end(bufferBuilder, 0, 0, 0);
//        RenderType.glintTranslucent().setupRenderState();
//        RenderType.glintTranslucent().end(bufferBuilder, 0, 0, 0);

//        IRenderTypeBuffer bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        if (snapshotSize < 32) {
            // Calen
            BufferBuilder buffer = Tessellator.getInstance().getBuilder();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

//            TileEntityRendererDispatcher.instance.preDrawBatch();
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).offset(FakeWorld.BLUEPRINT_OFFSET);
//                        // Calen test
//                        BlockPos pos = new BlockPos(0, 0, 0);
//                        GlStateManager.pushAttrib();
//                        // noinspection ConstantConditions
//                        TileEntityRendererDispatcher.instance.render(
//                            world.getBlockEntity(pos),
//                            pos.getX() - FakeWorld.BLUEPRINT_OFFSET.getX(),
//                            pos.getY() - FakeWorld.BLUEPRINT_OFFSET.getY(),
//                            pos.getZ() - FakeWorld.BLUEPRINT_OFFSET.getZ(),
//                            0
//                        );
                        poseStack.pushPose();
                        // TODO Calen translate should pos.get_()???
                        poseStack.translate(
                                pos.getX() - FakeWorld.BLUEPRINT_OFFSET.getX(),
                                pos.getY() - FakeWorld.BLUEPRINT_OFFSET.getY(),
                                pos.getZ() - FakeWorld.BLUEPRINT_OFFSET.getZ()
                        );
//                        // Calen test
//                        poseStack.translate(0, 0, 0);
//                        Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(
//                                world.getBlockEntity(pos),
//                                poseStack,
//                                bufferSource,
//                                0,
//                                OverlayTexture.NO_OVERLAY
//                        );
                        TileEntity te = world.getBlockEntity(pos);
                        if (te != null) {
                            TileEntityRendererDispatcher.instance.render(
//                                    world.getBlockEntity(pos),
                                    te,
                                    partialTicks,
                                    poseStack,
//                                    bufferSource
                                    (renderType) -> buffer
                            );
                        }
//                        GlStateManager.popAttrib();
                        poseStack.popPose();
                    }
                }
            }
//            TileEntityRendererDispatcher.instance.drawBatch(1);
            Tessellator.getInstance().end();
        }
        // noinspection Guava
        // Calen
        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

//        for (Entity entity : world.getEntities(Entity.class, Predicates.alwaysTrue()))
        for (Entity entity : world.getEntitiesOfClass(Entity.class, new AxisAlignedBB(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE), Predicates.alwaysTrue())) {
//            Vector3d pos = entity.getPositionVector();
            Vector3d pos = entity.position();
//            GlStateManager.pushAttrib();
//            Minecraft.getMinecraft().getRenderManager().renderEntity(
//                    entity,
//                    pos.x - FakeWorld.BLUEPRINT_OFFSET.getX(),
//                    pos.y - FakeWorld.BLUEPRINT_OFFSET.getY(),
//                    pos.z - FakeWorld.BLUEPRINT_OFFSET.getZ(),
//                    0,
//                    0,
//                    true
//            );
            // TODO Calen translate should pos.get_()???
            poseStack.pushPose();
            poseStack.translate(pos.x, pos.y, pos.z);
            Minecraft.getInstance().getEntityRenderDispatcher().render(
                    entity,
                    -FakeWorld.BLUEPRINT_OFFSET.getX(),
                    -FakeWorld.BLUEPRINT_OFFSET.getY(),
                    -FakeWorld.BLUEPRINT_OFFSET.getZ(),
                    0,
                    0,
                    poseStack,
//                    bufferSource,
                    (renderType) -> buffer,
                    0 // TODO Calen light 0???
            );
            poseStack.popPose();
//            GlStateManager.popAttrib();
        }
//        GlStateManager.popMatrix();
        poseStack.popPose();
        Tessellator.getInstance().end();
//        GlStateManager.disableRescaleNormal();
//        GlStateManager.matrixMode(GL11.GL_PROJECTION);
//        RenderSystem.getShader().PROJECTION_MATRIX.set(poseStack.last().pose());
        // TODO Calen debug
////        GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
//        GlStateManager._viewport(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight());
//        GlStateManager.popMatrix();
        poseStack.popPose();
//        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
//        RenderSystem.getShader().MODEL_VIEW_MATRIX.set(poseStack.last().pose());
//        GlStateManager.popMatrix();
        poseStack.popPose();
//        GlStateManager.disableBlend();
        RenderSystem.disableBlend();
//        GlStateManager.disableDepth();
        RenderSystem.disableDepthTest();
//        GlStateManager.popAttrib();
    }
}
