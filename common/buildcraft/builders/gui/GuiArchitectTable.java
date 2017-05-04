/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;

import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;

import java.io.IOException;

public class GuiArchitectTable extends GuiBC8<ContainerArchitectTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/architect.png");
    private static final int SIZE_X = 256, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 0, 166, 24, 17);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(159, 34, 24, 17);

    private GuiTextField nameField;

    public GuiArchitectTable(ContainerArchitectTable container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        nameField = new GuiTextField(0, fontRenderer, guiLeft + 90, guiTop + 62, 156, 12);
        nameField.setText(container.tile.name);
        nameField.setFocused(true);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
        drawProgress(
                RECT_PROGRESS,
                ICON_PROGRESS,
                container.tile.deltaProgress.getDynamic(partialTicks),
                1
        );
    }

    @Override
    protected void drawForegroundLayer() {
        nameField.drawTextBox();
    }

    @Override
    public void updateScreen() {
        nameField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        boolean typed = false;
        if (nameField.isFocused()) {
            typed = nameField.textboxKeyTyped(typedChar, keyCode);
            container.sendNameToServer(nameField.getText().trim());
        }
        if (!typed) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
