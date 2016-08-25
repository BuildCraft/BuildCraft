/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerArchitect;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiArchitect extends GuiBC8<ContainerArchitect> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/architect.png");
    private static final int SIZE_X = 256, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS_ONE = new GuiIcon(TEXTURE_BASE, 0, 166, 24, 8);
    private static final GuiIcon ICON_PROGRESS_ALL = new GuiIcon(TEXTURE_BASE, 0, 174, 24, 9);
    private static final GuiRectangle RECT_PROGRESS_ONE = new GuiRectangle(159, 34, 24, 8);
    private static final GuiRectangle RECT_PROGRESS_ALL = new GuiRectangle(159, 42, 24, 9);

    private GuiTextField nameField; // TODO: sending to server

    public GuiArchitect(ContainerArchitect container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        nameField = new GuiTextField(0, fontRendererObj, 90, 62, 156, 12);
        nameField.setText(container.tile.name);
        nameField.setFocused(true);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
        ICON_PROGRESS_ONE.drawCutInside(RECT_PROGRESS_ONE.createProgress(container.tile.deltaProgress.getDynamic(partialTicks) / 100, 1).offset(rootElement));
        ICON_PROGRESS_ALL.drawCutInside(RECT_PROGRESS_ALL.createProgress(container.tile.getAllProgress() / 100, 1).offset(rootElement));
    }

    @Override
    protected void drawForegroundLayer() {
        GlStateManager.translate(guiLeft, guiTop, 0);
        nameField.drawTextBox();
        GlStateManager.translate(-guiLeft, -guiTop, 0);
    }
}
