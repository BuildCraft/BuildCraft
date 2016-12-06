/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.robotics.gui;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.zone.*;
import buildcraft.robotics.zone.ZonePlannerMapChunk.MapColourData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GuiZonePlanner extends GuiBC8<ContainerZonePlanner> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftrobotics:textures/gui/zone_planner.png");
    private static final int SIZE_X = 256, SIZE_Y = 228;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS_INPUT = new GuiIcon(TEXTURE_BASE, 9, 228, 28, 9);
    private static final GuiIcon ICON_PROGRESS_OUTPUT = new GuiIcon(TEXTURE_BASE, 0, 228, 9, 28);
    private static final GuiRectangle RECT_PROGRESS_INPUT = new GuiRectangle(44, 128, 28, 9);
    private static final GuiRectangle RECT_PROGRESS_OUTPUT = new GuiRectangle(236, 45, 9, 28);
    private float startMouseX = 0;
    private float startMouseY = 0;
    private float startPositionX = 0;
    private float startPositionZ = 0;
    private float camY = 256;
    private float scaleSpeed = 0;
    private float positionX = 0;
    private float positionZ = 0;
    private boolean canDrag = false;
    private BlockPos lastSelected = null;
    private BlockPos selectionStartXZ = null;
    private ZonePlan bufferLayer = null;

    public GuiZonePlanner(ContainerZonePlanner container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        BlockPos tilePos = container.tile.getPos();
        positionX = tilePos.getX();
        positionZ = tilePos.getZ();
    }

    private ItemStack getCurrentStack() {
        return mc.thePlayer.inventory.getItemStack();
    }

    private ItemStack getPaintbrush() {
        ItemStack currentStack = getCurrentStack();
        if(currentStack != null && currentStack.getItem() instanceof ItemPaintbrush_BC8) {
            return currentStack;
        }
        return null;
    }

    private ItemPaintbrush_BC8.Brush getPaintbrushBrush() {
        ItemStack paintbrush = getPaintbrush();
        if(paintbrush != null) {
            return BCCoreItems.paintbrush.getBrushFromStack(paintbrush);
        }
        return null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if(wheel != 0) {
            scaleSpeed -= wheel / 30F;
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        canDrag = false;
        if(getPaintbrush() != null) {
            if(lastSelected != null) {
                selectionStartXZ = lastSelected;
            }
        } else if(getCurrentStack() == null) {
            startPositionX = positionX;
            startPositionZ = positionZ;
            startMouseX = mouseX;
            startMouseY = mouseY;
            canDrag = true;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if(!canDrag) {
            if(lastSelected != null && getPaintbrushBrush() != null) {
                bufferLayer = new ZonePlan(container.tile.layers[getPaintbrushBrush().colour.getMetadata()]);
                if(selectionStartXZ != null && getPaintbrushBrush() != null && lastSelected != null) {
                    final ZonePlan layer = container.tile.layers[getPaintbrushBrush().colour.getMetadata()];
                    for(int x = Math.min(selectionStartXZ.getX(), lastSelected.getX()); x < Math.max(selectionStartXZ.getX(), lastSelected.getX()); x++) {
                        for(int z = Math.min(selectionStartXZ.getZ(), lastSelected.getZ()); z < Math.max(selectionStartXZ.getZ(), lastSelected.getZ()); z++) {
                            if(clickedMouseButton == 0) {
                                bufferLayer.set(x, z, true);
                            } else if(clickedMouseButton == 1) {
                                bufferLayer.set(x, z, false);
                            }
                        }
                    }
                }
            }
            return;
        }
        float deltaX = mouseX - startMouseX;
        float deltaY = mouseY - startMouseY;
        float s = 0.3F;
        positionX = startPositionX - deltaX * s;
        positionZ = startPositionZ - deltaY * s;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        selectionStartXZ = null;
        if(getPaintbrushBrush() != null && bufferLayer != null) {
            container.tile.layers[getPaintbrushBrush().colour.getMetadata()] = bufferLayer;
            container.tile.sendLayerToServer(getPaintbrushBrush().colour.getMetadata());
        }
        bufferLayer = null;
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        ICON_PROGRESS_INPUT.drawCutInside(RECT_PROGRESS_INPUT.createProgress(container.tile.deltaProgressInput.getDynamic(partialTicks) / 100, 1).offset(rootElement));
        ICON_PROGRESS_OUTPUT.drawCutInside(RECT_PROGRESS_OUTPUT.createProgress(1, container.tile.deltaProgressOutput.getDynamic(partialTicks) / 100).offset(rootElement));
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    @Override
    protected void drawForegroundLayer() {
        camY += scaleSpeed;
        scaleSpeed *= 0.7F;
        int posX = (int) positionX;
        int posZ = (int) positionZ;
        int dimension = Minecraft.getMinecraft().theWorld.provider.getDimension();
        {
            ChunkPos chunkPos = new ChunkPos(posX >> 4, posZ >> 4);
            ZonePlannerMapChunkKey key = new ZonePlannerMapChunkKey(chunkPos, dimension, container.tile.getLevel());
            ZonePlannerMapChunk chunk = ZonePlannerMapDataClient.INSTANCE.getLoadedChunk(key);
            BlockPos pos = null;
            if(chunk != null) {
                MapColourData data = chunk.getData(posX, posZ);
                if(data != null) {
                    pos = new BlockPos(posX, data.posY, posZ);
                }
            }
            if(pos != null && pos.getY() + 10 > camY) {
                camY = Math.max(camY, pos.getY() + 10);
            }
        }
        int x = guiLeft;
        int y = guiTop;
        if(lastSelected != null) {
            String text = "X: " + lastSelected.getX() + " Y: " + lastSelected.getY() + " Z: " + lastSelected.getZ();
            fontRendererObj.drawString(text, x + 130, y + 130, 0x404040);
        }
        int offsetX = 8;
        int offsetY = 9;
        int sizeX = 213;
        int sizeY = 100;
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT); // TODO: save depth buffer?
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.viewport(//
                (x + offsetX) * scaledResolution.getScaleFactor(),//
                Minecraft.getMinecraft().displayHeight - (sizeY + y + offsetY) * scaledResolution.getScaleFactor(),//
                sizeX * scaledResolution.getScaleFactor(),//
                sizeY * scaledResolution.getScaleFactor()//
        );
        GlStateManager.scale(scaledResolution.getScaleFactor(), scaledResolution.getScaleFactor(), 1);
        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 1F, 1000.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.rotate(90, 1, 0, 0); // look down
        GlStateManager.pushMatrix();
        GlStateManager.translate(-positionX, -camY, -positionZ);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        int chunkBaseX = posX >> 4;
        int chunkBaseZ = posZ >> 4;
        int radius = 8;
        for(int chunkX = chunkBaseX - radius; chunkX < chunkBaseX + radius; chunkX++) {
            for(int chunkZ = chunkBaseZ - radius; chunkZ < chunkBaseZ + radius; chunkZ++) {
                ZonePlannerMapChunkKey key = new ZonePlannerMapChunkKey(new ChunkPos(chunkX, chunkZ), dimension, container.tile.getLevel());
                int glId = ZonePlannerMapRenderer.INSTANCE.getChunkGlList(key);
                if(glId > 0) {
                    GlStateManager.callList(glId);
                }
            }
        }

        BlockPos found = null;
        int foundColor = 0;

        if(Mouse.getX() / scaledResolution.getScaleFactor() > x + offsetX && Mouse.getX() / scaledResolution.getScaleFactor() < x + offsetX + sizeX && scaledResolution.getScaledHeight() - Mouse.getY() / scaledResolution.getScaleFactor() > y
                + offsetY && scaledResolution.getScaledHeight() - Mouse.getY() / scaledResolution.getScaleFactor() < y + offsetY + sizeY) {
            FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
            FloatBuffer modelViewBuffer = BufferUtils.createFloatBuffer(16);
            IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);

            GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer);
            GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
            GlStateManager.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);

            FloatBuffer positionNearBuffer = BufferUtils.createFloatBuffer(3);
            FloatBuffer positionFarBuffer = BufferUtils.createFloatBuffer(3);

            GLU.gluUnProject(Mouse.getX(), Mouse.getY(), 0f, modelViewBuffer, projectionBuffer, viewportBuffer, positionNearBuffer);
            GLU.gluUnProject(Mouse.getX(), Mouse.getY(), 1f, modelViewBuffer, projectionBuffer, viewportBuffer, positionFarBuffer);

            Vector3d rayStart = new Vector3d(positionNearBuffer.get(0), positionNearBuffer.get(1), positionNearBuffer.get(2));
            Vector3d rayPosition = new Vector3d(rayStart);
            Vector3d rayDirection = new Vector3d(positionFarBuffer.get(0), positionFarBuffer.get(1), positionFarBuffer.get(2));
            rayDirection.sub(rayStart);
            rayDirection.normalize();
            Vector3d offset = new Vector3d(rayDirection);
            offset.scale(100);
            rayPosition.add(offset);
            rayDirection.negate();

            for(int i = 0; i < 100; i++) {
                int chunkX = (int) Math.round(rayPosition.getX()) >> 4;
                int chunkZ = (int) Math.round(rayPosition.getZ()) >> 4;
                ZonePlannerMapChunk zonePlannerMapChunk = ZonePlannerMapDataClient.INSTANCE.getLoadedChunk(new ZonePlannerMapChunkKey(new ChunkPos(chunkX, chunkZ), Minecraft.getMinecraft().theWorld.provider.getDimension(), container.tile.getLevel()));
                if(zonePlannerMapChunk != null) {
                    BlockPos pos = new BlockPos(Math.round(rayPosition.getX()) - chunkX * 16, Math.round(rayPosition.getY()), Math.round(rayPosition.getZ()) - chunkZ * 16);
                    MapColourData data = zonePlannerMapChunk.getData(pos.getX(), pos.getZ());
                    if(data != null && data.posY >= pos.getY()) {
                        found = new BlockPos(pos.getX() + chunkX * 16, data.posY, pos.getZ() + chunkZ * 16);
                        foundColor = data.colour;
                    }
                } else {
                    break;
                }
                rayPosition.add(rayDirection);
            }
        }

        if(found != null) {
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GlStateManager.glLineWidth(2);
            int r = (int) (((foundColor >> 16) & 0xFF) * 0.7);
            int g = (int) (((foundColor >> 8) & 0xFF) * 0.7);
            int b = (int) (((foundColor >> 0) & 0xFF) * 0.7);
            int a = 0x77;
            ZonePlannerMapRenderer.INSTANCE.setColor(r << 16 | g << 8 | b << 0 | a << 24);
            VertexBuffer builder = Tessellator.getInstance().getBuffer();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            ZonePlannerMapRenderer.INSTANCE.drawBlockCuboid(builder, found.getX(), found.getY(), found.getZ());
            Tessellator.getInstance().draw();
            GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
        }

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        for(int i = 0; i < container.tile.layers.length; i++) {
            if(getPaintbrushBrush() != null && getPaintbrushBrush().colour.getMetadata() != i) {
                continue;
            }
            ZonePlan layer = container.tile.layers[i];
            if(getPaintbrushBrush() != null && getPaintbrushBrush().colour.getMetadata() == i && bufferLayer != null) {
                layer = bufferLayer;
            }
            if(!layer.getChunkPoses().isEmpty()) {
                for(int chunkX = chunkBaseX - radius; chunkX < chunkBaseX + radius; chunkX++) {
                    for(int chunkZ = chunkBaseZ - radius; chunkZ < chunkBaseZ + radius; chunkZ++) {
                        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                        if(layer.hasChunk(chunkPos)) {
                            for(int blockX = chunkPos.getXStart(); blockX <= chunkPos.getXEnd(); blockX++) {
                                for(int blockZ = chunkPos.getZStart(); blockZ <= chunkPos.getZEnd(); blockZ++) {
                                    if(layer.get(blockX, blockZ)) {
                                        int height = 256;
                                        ZonePlannerMapChunk zonePlannerMapChunk = ZonePlannerMapDataClient.INSTANCE.getLoadedChunk(new
                                                ZonePlannerMapChunkKey(chunkPos, dimension, container.tile.getLevel()));
                                        if(zonePlannerMapChunk != null) {
                                            MapColourData data = zonePlannerMapChunk.getData(blockX, blockZ);
                                            if(data != null) {
                                                height = data.posY;
                                            }
                                        }
                                        int color = EnumDyeColor.byMetadata(i).getMapColor().colorValue;
                                        int r = (color >> 16) & 0xFF;
                                        int g = (color >> 8) & 0xFF;
                                        int b = (color >> 0) & 0xFF;
                                        int a = 0x55;
                                        ZonePlannerMapRenderer.INSTANCE.setColor(r << 16 | g << 8 | b << 0 | a << 24);
                                        VertexBuffer builder = Tessellator.getInstance().getBuffer();
                                        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                                        ZonePlannerMapRenderer.INSTANCE.drawBlockCuboid(builder, blockX, height + 0.1, blockZ, height, 0.6);
                                        Tessellator.getInstance().draw();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();

        lastSelected = found;
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
    }
}
