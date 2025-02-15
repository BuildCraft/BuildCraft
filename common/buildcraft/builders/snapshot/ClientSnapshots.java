/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.net.MessageManager;
import com.google.common.base.Predicates;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ClientSnapshots {
    INSTANCE;

    // Calen
    private static final int COMBINED_LIGHT = 0x00F0_00F0;

    private final List<Snapshot> snapshots = new ArrayList<>();
    private final List<Snapshot.Key> pending = new ArrayList<>();
    private final Map<Snapshot.Key, FakeWorld> worlds = new HashMap<>();
//    private final Map<Snapshot.Key, BufferBuilder> buffers = new HashMap<>();

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
    public void renderSnapshot(Snapshot.Header header, int offsetX, int offsetY, int sizeX, int sizeY) {
        if (header == null) {
            return;
        }
        Snapshot snapshot = getSnapshot(header.key);
        if (snapshot == null) {
            return;
        }
        renderSnapshot(snapshot, offsetX, offsetY, sizeX, sizeY);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSnapshot(Snapshot snapshot, int offsetX, int offsetY, int sizeX, int sizeY) {
        FakeWorld world = worlds.computeIfAbsent(snapshot.key, key -> {
            FakeWorld localWorld = new FakeWorld();
            localWorld.uploadSnapshot(snapshot);
            return localWorld;
        });

        float particleTicks = Minecraft.getInstance().getFrameTime();
        Minecraft minecraft = Minecraft.getInstance();

//        GlStateManager.pushAttrib();
//        GlStateManager.enableDepth();
//        GlStateManager.enableBlend();
//        GlStateManager.pushMatrix();

//        GlStateManager.matrixMode(GL11.GL_PROJECTION);
//        GlStateManager.pushMatrix();
        RenderSystem.backupProjectionMatrix();
//        GlStateManager.loadIdentity();
        Matrix4f projectionMatrix = new Matrix4f().identity();
//        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
//        int viewportX = offsetX * scaledResolution.getScaleFactor();
        double scaleFactor = minecraft.getWindow().getGuiScale();
        int viewportX = (int) (offsetX * scaleFactor);
//        int viewportY = Minecraft.getMinecraft().displayHeight - (sizeY + offsetY) * scaledResolution.getScaleFactor();
        int viewportY = (int) (minecraft.getWindow().getHeight() - (sizeY + offsetY) * scaleFactor);
//        int viewportWidth = sizeX * scaledResolution.getScaleFactor();
        int viewportWidth = (int) (sizeX * scaleFactor);
//        int viewportHeight = sizeY * scaledResolution.getScaleFactor();
        int viewportHeight = (int) (sizeY * scaleFactor);
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        GL11.glScissor(
//                viewportX,
//                viewportY,
//                viewportWidth,
//                viewportHeight
//        );
        RenderSystem.enableScissor(
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight
        );
//        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
//        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        RenderSystem.disableScissor();
//        GlStateManager.viewport(
//                viewportX,
//                viewportY,
//                viewportWidth,
//                viewportHeight
//        );
        RenderSystem.viewport(
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight
        );
        // Calen: don't scale! or the blocks will be too big
//        GlStateManager.scale(scaledResolution.getScaleFactor(), scaledResolution.getScaleFactor(), 1);
//        projectionMatrix.scale((float) scaleFactor, (float) scaleFactor, 1);
//        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 0.1F, 1000.0F);
        projectionMatrix.perspective(70.0F, (float) sizeX / sizeY, 0.1F, 1000.0F);
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);

//        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
//        GlStateManager.loadIdentity();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
//        GlStateManager.enableRescaleNormal();
//        GlStateManager.pushMatrix();
        modelViewStack.pushPose();
        RenderSystem.applyModelViewMatrix();
        int snapshotSize = Math.max(Math.max(snapshot.size.getX(), snapshot.size.getY()), snapshot.size.getY());
//        GlStateManager.translate(0, 0, -snapshotSize * 2F - 3);
        modelViewStack.translate(0, 0, -snapshotSize * 2F - 3);
//        GlStateManager.rotate(20, 1, 0, 0);
        modelViewStack.rotateAround(Axis.XP.rotationDegrees(20), 1, 0, 0);
//        GlStateManager.rotate((System.currentTimeMillis() % 3600) / 10F, 0, 1, 0);
        modelViewStack.rotateAround(Axis.YP.rotationDegrees((System.currentTimeMillis() % 3600) / 10F), 0, 1, 0);
//        GlStateManager.translate(-snapshot.size.getX() / 2F, -snapshot.size.getY() / 2F, -snapshot.size.getZ() / 2F);
        modelViewStack.translate(-snapshot.size.getX() / 2F, -snapshot.size.getY() / 2F, -snapshot.size.getZ() / 2F);
//        GlStateManager.translate(0, snapshotSize * 0.1F, 0);
        modelViewStack.translate(0, snapshotSize * 0.1F, 0);
//        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//        new WorldVertexBufferUploader().draw(bufferBuilder);

//        BufferBuilder bufferBuilder = buffers.computeIfAbsent(snapshot.key, key -> {
//            BufferBuilder localBuffer = new BufferBuilder(1024) {
//                @Override
//                public void reset() {
//                }
//            };
//            localBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//            for (int z = 0; z < snapshot.size.getZ(); z++) {
//                for (int y = 0; y < snapshot.size.getY(); y++) {
//                    for (int x = 0; x < snapshot.size.getX(); x++) {
//                        BlockPos pos = new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET);
//                        localBuffer.setTranslation(
//                                -FakeWorld.BLUEPRINT_OFFSET.getX(),
//                                -FakeWorld.BLUEPRINT_OFFSET.getY(),
//                                -FakeWorld.BLUEPRINT_OFFSET.getZ()
//                        );
//                        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(
//                                world.getBlockState(pos),
//                                pos,
//                                world,
//                                localBuffer
//                        );
//                        localBuffer.setTranslation(0, 0, 0);
//                    }
//                }
//            }
//            localBuffer.finishDrawing();
//            return localBuffer;
//        });

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();

        modelViewStack.pushPose();
        modelViewStack.translate(
                -FakeWorld.BLUEPRINT_OFFSET.getX(),
                -FakeWorld.BLUEPRINT_OFFSET.getY(),
                -FakeWorld.BLUEPRINT_OFFSET.getZ()
        );
        for (int z = 0; z < snapshot.size.getZ(); z++) {
            for (int y = 0; y < snapshot.size.getY(); y++) {
                for (int x = 0; x < snapshot.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).offset(FakeWorld.BLUEPRINT_OFFSET);
                    modelViewStack.pushPose();
                    modelViewStack.translate(pos.getX(), pos.getY(), pos.getZ());
                    BlockState state = world.getBlockState(pos);
                    BakedModel model = blockRenderer.getBlockModel(state);
                    ModelData modelData;
                    BlockEntity te = world.getBlockEntity(pos);
                    if (te != null) {
                        modelData = te.getModelData();
                    } else {
                        modelData = ModelData.EMPTY;
                    }
                    modelData = model.getModelData(world, pos, state, modelData);
                    blockRenderer.renderSingleBlock(state, modelViewStack, bufferSource, COMBINED_LIGHT, OverlayTexture.NO_OVERLAY, modelData, null);
                    modelViewStack.popPose();
                }
            }
        }
        modelViewStack.popPose();

        if (snapshotSize < 32) {
            BlockEntityRenderDispatcher teRenderer = minecraft.getBlockEntityRenderDispatcher();
            modelViewStack.pushPose();
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).offset(FakeWorld.BLUEPRINT_OFFSET);
                        modelViewStack.pushPose();
                        modelViewStack.translate(
                                pos.getX() - FakeWorld.BLUEPRINT_OFFSET.getX(),
                                pos.getY() - FakeWorld.BLUEPRINT_OFFSET.getY(),
                                pos.getZ() - FakeWorld.BLUEPRINT_OFFSET.getZ()
                        );
                        // noinspection ConstantConditions
                        BlockEntity te = world.getBlockEntity(pos);
                        if (te == null) {
                            modelViewStack.popPose();
                            continue;
                        }
                        BlockEntityRenderer tesr = teRenderer.getRenderer(te);
                        tesr.render(
                                te,
                                particleTicks,
                                modelViewStack,
                                bufferSource,
//                                RenderUtil.getCombinedLight(world, pos),
                                COMBINED_LIGHT,
                                OverlayTexture.NO_OVERLAY
                        );
                        modelViewStack.popPose();
                    }
                }
            }
            modelViewStack.popPose();
        }
        // noinspection Guava
        EntityRenderDispatcher entityRenderer = minecraft.getEntityRenderDispatcher();
        for (Entity entity : world.getEntitiesOfClass(Entity.class, AABB.of(BoundingBox.infinite()), Predicates.alwaysTrue())) {
            Vec3 pos = entity.getPosition(0);
//            GlStateManager.pushAttrib();
            entityRenderer.render(
                    entity,
                    pos.x - FakeWorld.BLUEPRINT_OFFSET.getX(),
                    pos.y - FakeWorld.BLUEPRINT_OFFSET.getY(),
                    pos.z - FakeWorld.BLUEPRINT_OFFSET.getZ(),
                    0,
                    0,
                    modelViewStack,
                    bufferSource,
                    0
            );
//            GlStateManager.popAttrib();
        }

        bufferSource.endBatch();

//        GlStateManager.popMatrix();
        modelViewStack.popPose();
//        GlStateManager.disableRescaleNormal();
//        GlStateManager.matrixMode(GL11.GL_PROJECTION);
//        GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        RenderSystem.viewport(0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
//        GlStateManager.popMatrix();
        RenderSystem.restoreProjectionMatrix();
//        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
//        GlStateManager.popMatrix();
        modelViewStack.popPose();
//        GlStateManager.disableBlend();
//        GlStateManager.disableDepth();
//        GlStateManager.popAttrib();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }
}
