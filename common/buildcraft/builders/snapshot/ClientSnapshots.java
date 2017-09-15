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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.net.MessageManager;

public enum ClientSnapshots {
    INSTANCE;

    private final List<Snapshot> snapshots = new ArrayList<>();
    private final List<Snapshot.Key> pending = new ArrayList<>();
    private final Map<Snapshot.Key, FakeWorld> worlds = new HashMap<>();
    private final Map<Snapshot.Key, VertexBuffer> buffers = new HashMap<>();

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

    @SideOnly(Side.CLIENT)
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

    @SideOnly(Side.CLIENT)
    public void renderSnapshot(Snapshot snapshot, int offsetX, int offsetY, int sizeX, int sizeY) {
        FakeWorld world = worlds.computeIfAbsent(snapshot.key, key -> {
            FakeWorld localWorld = new FakeWorld();
            localWorld.uploadSnapshot(snapshot);
            return localWorld;
        });
        VertexBuffer vertexBuffer = buffers.computeIfAbsent(snapshot.key, key -> {
            VertexBuffer localBuffer = new VertexBuffer(1024) {
                @Override
                public void reset() {
                }
            };
            localBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET);
                        localBuffer.setTranslation(
                            -FakeWorld.BLUEPRINT_OFFSET.getX(),
                            -FakeWorld.BLUEPRINT_OFFSET.getY(),
                            -FakeWorld.BLUEPRINT_OFFSET.getZ()
                        );
                        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(
                            world.getBlockState(pos),
                            pos,
                            world,
                            localBuffer
                        );
                        localBuffer.setTranslation(0, 0, 0);
                    }
                }
            }
            localBuffer.finishDrawing();
            return localBuffer;
        });
        GlStateManager.pushAttrib();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        int viewportX = offsetX * scaledResolution.getScaleFactor();
        int viewportY = Minecraft.getMinecraft().displayHeight - (sizeY + offsetY) * scaledResolution.getScaleFactor();
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
        GlStateManager.viewport(
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight
        );
        GlStateManager.scale(scaledResolution.getScaleFactor(), scaledResolution.getScaleFactor(), 1);
        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 0.1F, 1000.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        int snapshotSize = Math.max(Math.max(snapshot.size.getX(), snapshot.size.getY()), snapshot.size.getY());
        GlStateManager.translate(0, 0, -snapshotSize * 2F - 3);
        GlStateManager.rotate(20, 1, 0, 0);
        GlStateManager.rotate((System.currentTimeMillis() % 3600) / 10F, 0, 1, 0);
        GlStateManager.translate(-snapshot.size.getX() / 2F, -snapshot.size.getY() / 2F, -snapshot.size.getZ() / 2F);
        GlStateManager.translate(0, snapshotSize * 0.1F, 0);
        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        new WorldVertexBufferUploader().draw(vertexBuffer);
        if (snapshotSize < 32) {
            TileEntityRendererDispatcher.instance.preDrawBatch();
            for (int z = 0; z < snapshot.size.getZ(); z++) {
                for (int y = 0; y < snapshot.size.getY(); y++) {
                    for (int x = 0; x < snapshot.size.getX(); x++) {
                        BlockPos pos = new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET);
                        GlStateManager.pushAttrib();
                        // noinspection ConstantConditions
                        TileEntityRendererDispatcher.instance.renderTileEntityAt(
                            world.getTileEntity(pos),
                            pos.getX() - FakeWorld.BLUEPRINT_OFFSET.getX(),
                            pos.getY() - FakeWorld.BLUEPRINT_OFFSET.getY(),
                            pos.getZ() - FakeWorld.BLUEPRINT_OFFSET.getZ(),
                            0
                        );
                        GlStateManager.popAttrib();
                    }
                }
            }
            TileEntityRendererDispatcher.instance.drawBatch(1);
        }
        // noinspection Guava
        for (Entity entity : world.getEntities(Entity.class, Predicates.alwaysTrue())) {
            Vec3d pos = entity.getPositionVector();
            GlStateManager.pushAttrib();
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(
                entity,
                pos.xCoord - FakeWorld.BLUEPRINT_OFFSET.getX(),
                pos.yCoord - FakeWorld.BLUEPRINT_OFFSET.getY(),
                pos.zCoord - FakeWorld.BLUEPRINT_OFFSET.getZ(),
                0,
                0,
                true
            );
            GlStateManager.popAttrib();
        }
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.popAttrib();
    }
}
