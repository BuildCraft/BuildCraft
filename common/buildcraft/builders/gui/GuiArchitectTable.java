/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.container.ContainerArchitectTable;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiArchitectTable extends GuiBC8<ContainerArchitectTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/architect.png");
    private static final int SIZE_X = 256, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 0, 166, 24, 17);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(159, 34, 24, 17);

    // private GuiTextField nameField;
    private EditBox nameField;

    public GuiArchitectTable(ContainerArchitectTable container, Inventory inventory, Component component) {
        super(container, inventory, component);
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
//        nameField = new GuiTextField(0, fontRenderer, guiLeft + 90, guiTop + 62, 156, 12);
//        nameField = new EditBox(font, leftPos + 90, topPos + 62, 156, 12, Component.literal(container.tile.name));
        nameField = new EditBox(font, 90, 62, 156, 12, Component.literal(""));
//        nameField.setText(container.tile.name);
        nameField.setValue(container.tile.name);
        nameField.setFocused(true);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, GuiGraphics guiGraphics) {
        ICON_GUI.drawAt(mainGui.rootElement, guiGraphics);
        drawProgress(
                RECT_PROGRESS,
                ICON_PROGRESS,
                guiGraphics,
                container.tile.deltaProgress.getDynamic(partialTicks),
                1
        );
    }

    @Override
//    protected void drawForegroundLayer()
    protected void drawForegroundLayer(GuiGraphics guiGraphics) {
        // Calen
        String title = LocaleUtil.localize(BCBuildersBlocks.architect.get().getDescriptionId());
        guiGraphics.drawString(font, title, leftPos + (float) (imageWidth - font.width(title)) / 2, topPos + 5, 0x404040, false);

//        nameField.drawTextBox();
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(leftPos, topPos, 0);
        nameField.renderWidget(guiGraphics, 0, 0, Minecraft.getInstance().getFrameTime());
        poseStack.popPose();
    }

    @Override
//    public void updateScreen()
    public void containerTick() {
        // Calen FIXED: in 1.12.2 without super.containerTick(), the ledgers will not spread
        super.containerTick();
//        nameField.updateCursorCounter();
        nameField.tick();
    }

    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException
//    public boolean charTyped(char typedChar, int keyCode)
    public boolean keyPressed(int typedChar, int keyCode, int modifiers) {
        boolean typed = false;
        if (nameField.isFocused()) {
//            typed = nameField.textboxKeyTyped(typedChar, keyCode);
            typed = nameField.keyPressed(typedChar, keyCode, modifiers);
//            container.sendNameToServer(nameField.getText().trim());
            container.sendNameToServer(nameField.getValue().trim());
        }
        if (!typed) {
//            super.keyTyped(typedChar, keyCode);
            return super.keyPressed(typedChar, keyCode, modifiers);
        } else {
            return true;
        }
    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        boolean typed = false;
        if (nameField.isFocused()) {
//            typed = nameField.textboxKeyTyped(typedChar, keyCode);
            typed = nameField.charTyped(typedChar, keyCode);
//            container.sendNameToServer(nameField.getText().trim());
            container.sendNameToServer(nameField.getValue().trim());
        }
        if (!typed) {
//            super.keyTyped(typedChar, keyCode);
            return super.charTyped(typedChar, keyCode);
        } else {
            return true;
        }
    }

    @Override
//    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
//        return nameField.mouseClicked(mouseX, mouseY, mouseButton);
        return nameField.mouseClicked(mouseX - leftPos, mouseY - topPos, mouseButton);
    }
}
