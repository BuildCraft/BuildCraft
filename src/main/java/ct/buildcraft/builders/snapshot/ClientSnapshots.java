/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Quaternion;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.net.MessageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

public enum ClientSnapshots {
    INSTANCE;

    private final List<Snapshot> snapshots = new ArrayList<>();
    private final List<Snapshot.Key> pending = new ArrayList<>();
    private final Map<Snapshot.Key, FakeWorld> worlds = new HashMap<>();
    private final Map<Snapshot.Key, RenderedBuffer> buffers = new HashMap<>();

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
    public void renderSnapshot(PoseStack pose, Snapshot.Header header, int offsetX, int offsetY, int sizeX, int sizeY) {
        if (header == null) {
            return;
        }
        Snapshot snapshot = getSnapshot(header.key);
        if (snapshot == null) {
            return;
        }
        renderSnapshot(pose, snapshot, offsetX, offsetY, sizeX, sizeY);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderSnapshot(PoseStack pose, Snapshot snapshot, int offsetX, int offsetY, int sizeX, int sizeY) {
        if(1 == 1) {
        	return;
        }
    	FakeWorld world = worlds.computeIfAbsent(snapshot.key, key -> {
        	Minecraft mc = Minecraft.getInstance();
            FakeWorld localWorld = new FakeWorld(mc.level);
            localWorld.uploadSnapshot(snapshot);
            return localWorld;
        });
        //Minecraft.getInstance().renderBuffers()
        RenderedBuffer bufferBuilder = buffers.computeIfAbsent(snapshot.key, key -> {
            BufferBuilder localBuffer = new BufferBuilder(1024);
            localBuffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            pose.pushPose();
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).offset(FakeWorld.BLUEPRINT_OFFSET);
                        pose.translate(
                            -FakeWorld.BLUEPRINT_OFFSET.getX(),
                            -FakeWorld.BLUEPRINT_OFFSET.getY(),
                            -FakeWorld.BLUEPRINT_OFFSET.getZ()
                        );
                        BlockState blockState = world.getBlockState(pos);
                        BCLog.d("" + blockState);
						Minecraft.getInstance().getBlockRenderer().renderBatched(
                            blockState,
                            pos,
                            world,
                            pose, localBuffer, false, world.random, ModelData.EMPTY, RenderType.cutout()
                        );
                        pose.translate(0, 0, 0);
                    }
                }
            }
            pose.popPose();
            return localBuffer.end();
        });
        pose.pushPose();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
 //       GlStateManager.pushMatrix();
//       RenderSystem.matrixMode(GL11.GL_PROJECTION);
//        GlStateManager.pushMatrix();
//        GlStateManager.loadIdentity();
        Window window = Minecraft.getInstance().getWindow();
        int viewportX = (int) (offsetX * window.getGuiScale());
        int viewportY = (int) (window.getHeight() - (sizeY + offsetY) * window.getGuiScale());
        int viewportWidth = (int) (sizeX * window.getGuiScale());
        int viewportHeight = (int) (sizeY * window.getGuiScale());
        RenderSystem.enableScissor(viewportX, viewportY, viewportWidth, viewportHeight);
        GL11.glScissor(
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight
        );
//        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT);
        RenderSystem.disableScissor();
        RenderSystem.viewport(viewportX, viewportY, viewportWidth, viewportHeight);
 //       RenderSystem.scale(window.getGuiScale(), window.getGuiScale(), 1);
//        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 0.1F, 1000.0F);
/*        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();*/
        int snapshotSize = Math.max(Math.max(snapshot.size.getX(), snapshot.size.getY()), snapshot.size.getY());
        pose.translate(0, 0, -snapshotSize * 2d - 3);
        pose.mulPose(new Quaternion(20, 1, 0, 0));
        pose.mulPose(new Quaternion((System.currentTimeMillis() % 3600) / 10F, 0, 1, 0));
        pose.translate(-snapshot.size.getX() / 2F, -snapshot.size.getY() / 2F, -snapshot.size.getZ() / 2F);
        pose.translate(0, snapshotSize * 0.1F, 0);
        RenderSystem.setShaderTexture(snapshotSize, InventoryMenu.BLOCK_ATLAS);
 //       
        RenderSystem.setShader(GameRenderer::getBlockShader);
        BufferUploader.drawWithShader(bufferBuilder);
        if (snapshotSize < 32) {
//            Minecraft.getInstance().getBlockEntityRenderDispatcher().();
        	MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).offset(FakeWorld.BLUEPRINT_OFFSET);
                        pose.pushPose();
                        pose.translate(
                        		pos.getX() - FakeWorld.BLUEPRINT_OFFSET.getX(),
                        		pos.getY() - FakeWorld.BLUEPRINT_OFFSET.getY(),
                        		pos.getZ() - FakeWorld.BLUEPRINT_OFFSET.getZ());
                        // noinspection ConstantConditions
                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if(blockEntity != null)
						Minecraft.getInstance().getBlockEntityRenderDispatcher().render(
                            blockEntity,
                            0,
                            pose,
                            multibuffersource$buffersource
                        );
                        pose.popPose();
                    }
                }
            }
            multibuffersource$buffersource.endBatch();
//            TileEntityRendererDispatcher.instance.drawBatch(1);
        }
        // noinspection Guava
/*        for (Entity entity : world.getEntitiesOfClass(Entity.class, AABB.ofSize(Vec3.ZERO, viewportWidth, viewportHeight, snapshotSize))) {
            Vec3 pos = entity.position();
            //GlStateManager.pushAttrib();
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            pose.pushPose();
            Minecraft.getInstance().getEntityRenderDispatcher().render(
                entity,
                pos.x - FakeWorld.BLUEPRINT_OFFSET.getX(),
                pos.y - FakeWorld.BLUEPRINT_OFFSET.getY(),
                pos.z - FakeWorld.BLUEPRINT_OFFSET.getZ(),
                0,
                0,
                pose,
                multibuffersource$buffersource,
                15728880
            );
            pose.popPose();
            multibuffersource$buffersource.endBatch();
           // GlStateManager.popAttrib();
        }*///TODO
/*        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.viewport(0, 0, Minecraft.getInstance().displayWidth, Minecraft.getMinecraft().displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();*/
        var win = Minecraft.getInstance().getWindow();
        RenderSystem.viewport(0, 0, win.getWidth(), win.getHeight());
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        pose.popPose();
        buffers.remove(snapshot.key);//FORTEST
    }
}
