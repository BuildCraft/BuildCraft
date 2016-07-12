/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.robotics.gui;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.robotics.container.ContainerZonePlanner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.GLU;

import java.io.IOException;

public class GuiZonePlanner extends GuiBC8<ContainerZonePlanner> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftrobotics:textures/gui/zone_planner.png");
    private static final int SIZE_X = 256, SIZE_Y = 228;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private int listIndex = 0;
    private float startMouseX = 0;
    private float startMouseY = 0;
    private float startPositionX = 0;
    private float startPositionZ = 0;
    private float camY = 100;
    private float scaleSpeed = 0;
    private float positionX = 0;
    private float positionZ = 0;

    public GuiZonePlanner(ContainerZonePlanner container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    private void drawMap() {
        listIndex = GL11.glGenLists(1);
        GL11.glNewList(listIndex, GL11.GL_COMPILE);
        BlockPos tilePos = container.tile.getPos();
        VertexBuffer vertexBuffer = new VertexBuffer(2097152);
        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        final World world = container.tile.getWorld();
        for(int currentX = -64; currentX < 64; currentX++) {
            for(int currentZ = -64; currentZ < 64; currentZ++) {
                int yDrawed = 0;
                for(int currentY = world.getHeight(); currentY > 0 && yDrawed < 30; currentY--) {
                    BlockPos pos = new BlockPos(tilePos.getX() + currentX, currentY, tilePos.getZ() + currentZ);
                    if(world.isAirBlock(pos)) {
                        continue;
                    }
                    yDrawed++;
                    IBlockState state = world.getBlockState(pos);
                    Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(state, /*new BlockPos(0, 0, 0)*/ pos, world, vertexBuffer);
                }
            }
        }
        vertexBuffer.finishDrawing();
        new WorldVertexBufferUploader().draw(vertexBuffer);
        GL11.glEndList();
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scaleSpeed -= wheel / 50F;
        }
        super.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        startPositionX = positionX;
        startPositionZ = positionZ;
        startMouseX = mouseX;
        startMouseY = mouseY;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        float deltaX = startMouseX - mouseX;
        float deltaY = startMouseY - mouseY;
        float s = 0.3F;
        positionX = startPositionX - deltaX * s;
        positionZ = startPositionZ - deltaY * s;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }

    @Override
    protected void drawForegroundLayer() {
        camY += scaleSpeed;
        scaleSpeed *= 0.7F;
        int x = guiLeft;
        int y = guiTop;
        if(listIndex == 0) {
            drawMap();
        }
        int offsetX = 8;
        int offsetY = 9;
        int sizeX = 213;
        int sizeY = 100;
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT/* | GL11.GL_COLOR_BUFFER_BIT*/); // TODO: remove
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
        GLU.gluPerspective(70.0F, (float) sizeX / sizeY, 0.01F, 500.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glRotatef(90, 1, 0, 0);
        BlockPos tilePos = container.tile.getPos();
        GL11.glTranslatef(-tilePos.getX() + positionX, -camY, -tilePos.getZ() + positionZ);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND); // FIXME: blending
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glCallList(listIndex);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glViewport(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        RenderHelper.disableStandardItemLighting();
//        String title = I18n.format("tile.filteredBufferBlock.name");
//        int xPos = (xSize - fontRendererObj.getStringWidth(title)) / 2;
//        fontRendererObj.drawString(title, x + xPos, y + 10, 0x404040);
    }
}
