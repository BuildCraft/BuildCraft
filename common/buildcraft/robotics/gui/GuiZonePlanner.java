/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.robotics.gui;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.robotics.ZonePlan;
import buildcraft.robotics.ZonePlannerMapChunk;
import buildcraft.robotics.ZonePlannerMapDataClient;
import buildcraft.robotics.ZonePlannerMapRenderer;
import buildcraft.robotics.container.ContainerZonePlanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector3d;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GuiZonePlanner extends GuiBC8<ContainerZonePlanner> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftrobotics:textures/gui/zone_planner.png");
    private static final int SIZE_X = 256, SIZE_Y = 228;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
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
        if (wheel != 0) {
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
                            bufferLayer.set(x, z, !layer.get(x, z));
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
        }
        bufferLayer = null;
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    @Override
    protected void drawForegroundLayer() {
        camY += scaleSpeed;
        scaleSpeed *= 0.7F;
        int x = guiLeft;
        int y = guiTop;
        int offsetX = 8;
        int offsetY = 9;
        int sizeX = 213;
        int sizeY = 100;
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT); // TODO: save depth buffer?
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glViewport(
                (x + offsetX) * scaledResolution.getScaleFactor(),
                Minecraft.getMinecraft().displayHeight - (sizeY + y + offsetY) * scaledResolution.getScaleFactor(),
                sizeX * scaledResolution.getScaleFactor(),
                sizeY * scaledResolution.getScaleFactor()
        );
        GL11.glScalef(scaledResolution.getScaleFactor(), scaledResolution.getScaleFactor(), 1);
        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 1F, 1000.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glRotatef(90, 1, 0, 0); // look down
        GL11.glPushMatrix();
        GL11.glTranslatef(-positionX, -camY, -positionZ);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation("buildcraft", "block.png"));
        int chunkBaseX = (int)positionX >> 4;
        int chunkBaseZ = (int)positionZ >> 4;
        int radius = 8;
        for(int chunkX = chunkBaseX - radius; chunkX < chunkBaseX + radius; chunkX++) {
            for(int chunkZ = chunkBaseZ - radius; chunkZ < chunkBaseZ + radius; chunkZ++) {
                GL11.glCallList(ZonePlannerMapRenderer.instance.drawChunk(container.tile.getWorld(), new ChunkPos(chunkX, chunkZ)));
            }
        }

        FloatBuffer projectionBuffer = BufferUtils.createFloatBuffer(16);
        FloatBuffer modelViewBuffer = BufferUtils.createFloatBuffer(16);
        IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);

        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionBuffer);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);

        FloatBuffer positionNearBuffer = BufferUtils.createFloatBuffer(3);
        FloatBuffer positionFarBuffer = BufferUtils.createFloatBuffer(3);

        GLU.gluUnProject(Mouse.getX(), Mouse.getY(), 0f, modelViewBuffer, projectionBuffer, viewportBuffer, positionNearBuffer);
        GLU.gluUnProject(Mouse.getX(), Mouse.getY(), 1f, modelViewBuffer, projectionBuffer, viewportBuffer, positionFarBuffer);

        Vector3d rayStart = new Vector3d(positionNearBuffer.get(0), positionNearBuffer.get(1), positionNearBuffer.get(2));
        Vector3d rayPosition = new Vector3d(rayStart);
        Vector3d rayDirection = new Vector3d(positionFarBuffer.get(0), positionFarBuffer.get(1), positionFarBuffer.get(2));
        rayDirection.sub(rayStart);
        rayDirection.normalize();
        rayDirection.scale(0.1);
        BlockPos found = null;
        int foundColor = 0;

        for(int i = 0; i < 10000; i++) {
            int chunkX = (int)rayPosition.getX() >> 4;
            int chunkZ = (int)rayPosition.getZ() >> 4;
            ZonePlannerMapChunk zonePlannerMapChunk = ZonePlannerMapDataClient.instance.getLoadedChunk(new ChunkPos(chunkX, chunkZ));
            if(zonePlannerMapChunk != null) {
                BlockPos pos = new BlockPos(Math.round(rayPosition.getX()) - chunkX * 16, Math.round(rayPosition.getY()), Math.round(rayPosition.getZ()) - chunkZ * 16);
                if(zonePlannerMapChunk.data.containsKey(pos)) {
                    found = new BlockPos(pos.getX() + chunkX * 16, pos.getY(), pos.getZ() + chunkZ * 16);
                    foundColor = zonePlannerMapChunk.data.get(pos);
                    break;
                }
            } else {
                break;
            }
            rayPosition.add(rayDirection);
        }

        if(found != null) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GL11.glBegin(GL11.GL_QUADS);
            int r = (foundColor >> 16) & 0xFF;
            int g = (foundColor >> 8) & 0xFF;
            int b = (foundColor >> 0) & 0xFF;
            //noinspection unused
            int a = (foundColor >> 24) & 0xFF;
            GL11.glColor4d(r / (double)0xFF + 0.3, g / (double)0xFF + 0.3, b / (double)0xFF + 0.3, 0.7);
            ZonePlannerMapRenderer.instance.drawBlockCuboid(found.getX(), found.getY(), found.getZ());
            GL11.glEnd();
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBegin(GL11.GL_QUADS);

        for(int i = 0; i < container.tile.layers.length; i++) {
            if(getPaintbrushBrush() != null && getPaintbrushBrush().colour.getMetadata() != i) {
                continue;
            }
            ZonePlan layer = container.tile.layers[i];
            if(getPaintbrushBrush() != null && getPaintbrushBrush().colour.getMetadata() == i && bufferLayer != null) {
                layer = bufferLayer;
            }
            for(int chunkX = chunkBaseX - radius; chunkX < chunkBaseX + radius; chunkX++) {
                for(int chunkZ = chunkBaseZ - radius; chunkZ < chunkBaseZ + radius; chunkZ++) {
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                    for(int blockX = chunkPos.getXStart(); blockX <= chunkPos.getXEnd(); blockX++) {
                        for(int blockZ = chunkPos.getZStart(); blockZ <= chunkPos.getZEnd(); blockZ++) {
                            if(layer.get(blockX, blockZ)) {
                                int height = 256;
                                ZonePlannerMapChunk zonePlannerMapChunk = ZonePlannerMapDataClient.instance.getLoadedChunk(chunkPos);
                                if(zonePlannerMapChunk != null) {
                                    int finalBlockX = blockX;
                                    int finalBlockZ = blockZ;
                                    BlockPos pos = zonePlannerMapChunk.data.keySet().stream().filter(blockPos -> {
                                        //noinspection CodeBlock2Expr // it's too long
                                        return blockPos.getX() == finalBlockX - chunkPos.chunkXPos * 16 && blockPos.getZ() == finalBlockZ - chunkPos.chunkZPos * 16;
                                    }).findFirst().orElse(null);
                                    if(pos != null) {
                                        height = pos.getY();
                                    }
                                }
                                int color = EnumDyeColor.byMetadata(i).getMapColor().colorValue;
                                int r = (color >> 16) & 0xFF;
                                int g = (color >> 8) & 0xFF;
                                int b = (color >> 0) & 0xFF;
                                //noinspection unused
                                int a = (color >> 24) & 0xFF;
                                GL11.glColor4d(r / (double)0xFF, g / (double)0xFF, b / (double)0xFF, 0.3);
                                ZonePlannerMapRenderer.instance.drawBlockCuboid(blockX, height + 0.1, blockZ, height, 0.6);
                            }
                        }
                    }
                }
            }
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        lastSelected = found;
        GL11.glPopMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glColor4d(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_BLEND);
    }
}