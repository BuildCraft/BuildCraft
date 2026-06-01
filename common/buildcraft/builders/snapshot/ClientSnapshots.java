/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicates;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.render.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.net.MessageManager;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import buildcraft.lib.misc.GlStateManagerCompat;
import net.minecraft.client.render.VertexFormat;

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

    @Environment(EnvType.CLIENT)
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

    @Environment(EnvType.CLIENT)
    public void renderSnapshot(Snapshot snapshot, int offsetX, int offsetY, int sizeX, int sizeY) {
        FakeWorld world = worlds.computeIfAbsent(snapshot.key, key -> {
            FakeWorld localWorld = new FakeWorld();
            localWorld.uploadSnapshot(snapshot);
            return localWorld;
        });
        BufferBuilder bufferBuilder = buffers.computeIfAbsent(snapshot.key, key -> {
            BufferBuilder localBuffer = new BufferBuilder(1024) {
                @Override
                public void reset() {
                }
            };
            localBuffer.begin(VertexFormat.DrawMode.QUADS, DefaultVertexFormats.BLOCK);
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET);
                        // TODO(R.Chen): setTranslation removed — use MatrixStack instead:
                        // localBuffer.setTranslation(-FakeWorld.BLUEPRINT_OFFSET.getX(),
                        //     -FakeWorld.BLUEPRINT_OFFSET.getY(), -FakeWorld.BLUEPRINT_OFFSET.getZ());
                        MinecraftClient.getInstance().getBlockRendererDispatcher().renderBlock(
                            world.getBlockState(pos),
                            pos,
                            world,
                            localBuffer
                        );
                        // TODO(R.Chen): setTranslation removed — use MatrixStack instead: localBuffer.setTranslation(0, 0, 0);
                    }
                }
            }
            localBuffer.finishDrawing();
            return localBuffer;
        });
        GlStateManagerCompat.pushAttrib();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.getModelViewStack().push();
        GlStateManagerCompat.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.getModelViewStack().push();
        GlStateManagerCompat.loadIdentity();
        ScaledResolution scaledResolution = new ScaledResolution(MinecraftClient.getInstance());
        int viewportX = offsetX * scaledResolution.getScaleFactor();
        int viewportY = MinecraftClient.getInstance().getWindow().getHeight() - (sizeY + offsetY) * scaledResolution.getScaleFactor();
        int viewportWidth = sizeX * scaledResolution.getScaleFactor();
        int viewportHeight = sizeY * scaledResolution.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight
        );
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManagerCompat.viewport(
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight
        );
        RenderSystem.getModelViewStack().scale(scaledResolution.getScaleFactor(), scaledResolution.getScaleFactor(), 1);
        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 0.1F, 1000.0F);
        GlStateManagerCompat.matrixMode(GL11.GL_MODELVIEW);
        GlStateManagerCompat.loadIdentity();
        GlStateManagerCompat.enableRescaleNormal();
        RenderSystem.getModelViewStack().push();
        int snapshotSize = Math.max(Math.max(snapshot.size.getX(), snapshot.size.getY()), snapshot.size.getY());
        RenderSystem.getModelViewStack().translate(0, 0, -snapshotSize * 2F - 3);
        RenderSystem.getModelViewStack().rotate(20, 1, 0, 0);
        RenderSystem.getModelViewStack().rotate((System.currentTimeMillis() % 3600) / 10F, 0, 1, 0);
        RenderSystem.getModelViewStack().translate(-snapshot.size.getX() / 2F, -snapshot.size.getY() / 2F, -snapshot.size.getZ() / 2F);
        RenderSystem.getModelViewStack().translate(0, snapshotSize * 0.1F, 0);
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, net.minecraft.screen.PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        new WorldVertexBufferUploader().draw(bufferBuilder);
        if (snapshotSize < 32) {
            TileEntityRendererDispatcher.instance.preDrawBatch();
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET);
                        GlStateManagerCompat.pushAttrib();
                        // noinspection ConstantConditions
                        TileEntityRendererDispatcher.instance.render(
                            world.getBlockEntity(pos),
                            pos.getX() - FakeWorld.BLUEPRINT_OFFSET.getX(),
                            pos.getY() - FakeWorld.BLUEPRINT_OFFSET.getY(),
                            pos.getZ() - FakeWorld.BLUEPRINT_OFFSET.getZ(),
                            0
                        );
                        GlStateManagerCompat.popAttrib();
                    }
                }
            }
            TileEntityRendererDispatcher.instance.drawBatch(1);
        }
        // noinspection Guava
        for (Entity entity : world.getEntities(Entity.class, Predicates.alwaysTrue())) {
            Vec3d pos = entity.getPos();
            GlStateManagerCompat.pushAttrib();
            MinecraftClient.getInstance().getRenderManager().renderEntity(
                entity,
                pos.x - FakeWorld.BLUEPRINT_OFFSET.getX(),
                pos.y - FakeWorld.BLUEPRINT_OFFSET.getY(),
                pos.z - FakeWorld.BLUEPRINT_OFFSET.getZ(),
                0,
                0,
                true
            );
            GlStateManagerCompat.popAttrib();
        }
        RenderSystem.getModelViewStack().pop();
        GlStateManagerCompat.disableRescaleNormal();
        GlStateManagerCompat.matrixMode(GL11.GL_PROJECTION);
        GlStateManagerCompat.viewport(0, 0, MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight());
        RenderSystem.getModelViewStack().pop();
        GlStateManagerCompat.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.getModelViewStack().pop();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        GlStateManagerCompat.popAttrib();
    }
}
