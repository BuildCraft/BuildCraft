/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.container.ContainerReplacer;
import buildcraft.builders.snapshot.ClientSnapshots;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiReplacer extends GuiBC8<ContainerReplacer> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftbuilders:textures/gui/replacer.png");
    private static final int SIZE_X = 176, SIZE_Y = 241;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    //    private GuiTextField nameField;
    private EditBox nameField;

    public GuiReplacer(ContainerReplacer container, Inventory inventory, Component component) {
        super(container, inventory, component);
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
    }

    @Override
    public void initGui() {
//        super.initGui();
//        nameField = new GuiTextField(0, fontRenderer, guiLeft + 30, guiTop + 117, 138, 12);
//        nameField = new EditBox(font, leftPos + 30, topPos + 117, 138, 12, Component.literal(""));
        nameField = new EditBox(font, 30, 117, 138, 12, Component.literal(""));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, GuiGraphics guiGraphics) {
        ICON_GUI.drawAt(mainGui.rootElement, guiGraphics);
        ClientSnapshots.INSTANCE.renderSnapshot(
//                BCBuildersItems.snapshotBLUEPRINT_CLEAN.get().getHeader(container.tile.invSnapshot.getStackInSlot(0)),
                BCBuildersItems.snapshotBLUEPRINT.get().getHeader(container.tile.invSnapshot.getStackInSlot(0)),
                leftPos + 8,
//                guiLeft + 8,
                topPos + 9,
//                guiTop + 9,
                160,
                100
        );
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics guiGraphics) {
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
        // Calen FIXED: in 1.12.2 without super.tick(), ledgers will not spread
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
            // container.sendNameToServer(nameField.getText().trim());
        }
        if (!typed) {
//            super.keyTyped(typedChar, keyCode);
            return super.keyPressed(typedChar, keyCode, modifiers);
        } else {
            return true;
        }
    }

    public boolean charTyped(char typedChar, int keyCode) {
        boolean typed = false;
        if (nameField.isFocused()) {
//            typed = nameField.textboxKeyTyped(typedChar, keyCode);
            typed = nameField.charTyped(typedChar, keyCode);
            // container.sendNameToServer(nameField.getText().trim());
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
//        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        return nameField.mouseClicked(mouseX - leftPos, mouseY - topPos, mouseButton);
    }
}
