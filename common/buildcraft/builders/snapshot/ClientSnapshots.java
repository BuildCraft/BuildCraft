/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.lib.net.MessageManager;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ClientSnapshots {
    INSTANCE;

    private final List<Snapshot> snapshots = new ArrayList<>();
    private final List<Snapshot.Header> pending = new ArrayList<>();
    private final Map<Snapshot.Header, FakeWorld> worlds = new HashMap<>();
    private final Map<Snapshot.Header, VertexBuffer> buffers = new HashMap<>();

    public Snapshot getSnapshot(Snapshot.Header header) {
        Snapshot found = snapshots.stream().filter(snapshot -> snapshot.header.equals(header)).findFirst().orElse(null);
        if (found == null && !pending.contains(header)) {
            pending.add(header);
            MessageManager.sendToServer(new MessageSnapshotRequest(header));
        }
        return found;
    }

    public void onSnapshotReceived(Snapshot snapshot) {
        pending.remove(snapshot.header);
        snapshots.add(snapshot);
    }

    @SideOnly(Side.CLIENT)
    public void renderSnapshot(Snapshot.Header header, int offsetX, int offsetY, int sizeX, int sizeY) {
        if (header == null) {
            return;
        }
        Snapshot snapshot = getSnapshot(header);
        if (snapshot == null) {
            return;
        }
        renderSnapshot(snapshot, offsetX, offsetY, sizeX, sizeY);
    }

    @SideOnly(Side.CLIENT)
    public void renderSnapshot(Snapshot snapshot, int offsetX, int offsetY, int sizeX, int sizeY) {
        FakeWorld world = worlds.computeIfAbsent(snapshot.header, localHeader -> {
            FakeWorld localWorld = new FakeWorld();
            if (snapshot instanceof Blueprint) {
                localWorld.uploadBlueprint((Blueprint) snapshot, false);
                for (ISchematicEntity<?> schematicEntity : ((Blueprint) snapshot).entities) {
                    schematicEntity.build(localWorld, FakeWorld.BLUEPRINT_OFFSET);
                }
            }
            if (snapshot instanceof Template) {
                for (int z = 0; z < snapshot.size.getZ(); z++) {
                    for (int y = 0; y < snapshot.size.getY(); y++) {
                        for (int x = 0; x < snapshot.size.getX(); x++) {
                            if (((Template) snapshot).data[x][y][z]) {
                                localWorld.setBlockState(
                                    new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET),
                                    Blocks.QUARTZ_BLOCK.getDefaultState()
                                );
                            }
                        }
                    }
                }
            }
            return localWorld;
        });
        VertexBuffer vertexBuffer = buffers.computeIfAbsent(snapshot.header, localHeader -> {
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
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT); // TODO: save depth buffer?
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.viewport(
            offsetX * scaledResolution.getScaleFactor(),
            Minecraft.getMinecraft().displayHeight - (sizeY + offsetY) * scaledResolution.getScaleFactor(),
            sizeX * scaledResolution.getScaleFactor(),
            sizeY * scaledResolution.getScaleFactor()
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
        }
        // noinspection Guava
        for (Entity entity : world.getEntities(Entity.class, Predicates.alwaysTrue())) {
            Vec3d pos = entity.getPositionVector();
            switch (snapshot.facing) {
                case NORTH:
                    pos = new Vec3d(
                        pos.xCoord + snapshot.size.getX() - 1,
                        pos.yCoord,
                        pos.zCoord
                    );
                    break;
                case SOUTH:
                    pos = new Vec3d(
                        pos.xCoord + snapshot.size.getX() - 1,
                        pos.yCoord,
                        pos.zCoord + snapshot.size.getZ() - 1
                    );
                    break;
                case WEST:
                    pos = new Vec3d(
                        pos.xCoord,
                        pos.yCoord,
                        pos.zCoord + snapshot.size.getZ() - 1
                    );
                    break;
                case EAST:
                    pos = new Vec3d(
                        pos.xCoord + snapshot.size.getX() - 1,
                        pos.yCoord,
                        pos.zCoord + snapshot.size.getZ() - 1
                    );
                    break;
            }
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
