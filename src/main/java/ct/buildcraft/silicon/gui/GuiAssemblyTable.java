/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.silicon.gui;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.lib.gui.pos.IGuiArea;
import ct.buildcraft.lib.gui.pos.IGuiPosition;
import ct.buildcraft.lib.gui.pos.PositionAbsolute;
import ct.buildcraft.silicon.EnumAssemblyRecipeState;
import ct.buildcraft.silicon.container.ContainerAssemblyTable;
import ct.buildcraft.silicon.tile.TileAssemblyTable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiAssemblyTable extends GuiBC8<ContainerAssemblyTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/assembly_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 220;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_SAVED = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH = new GuiIcon(TEXTURE_BASE, SIZE_X, 16, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH_ACTIVE = new GuiIcon(TEXTURE_BASE, SIZE_X, 32, 16, 16);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 48, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(86, 36, 4, 70);

    public GuiAssemblyTable(ContainerAssemblyTable container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
        titleLabelY += 8;
    }

    private IGuiPosition getPos(int index) {
        int posX = index % 3;
        int posY = index / 3;
        return new PositionAbsolute(116 + posX * 18, 36 + posY * 18);
    }

    private IGuiArea getArea(int index) {
        return index < 3 * 4
                ? new GuiRectangle(16, 16).offset(mainGui.rootElement).offset(getPos(index))
                : GuiRectangle.ZERO;
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);

        long target = container.tile.getTarget();
        if (target != 0) {
            double v = (double) container.tile.power / target;
            ICON_PROGRESS.drawCutInside(
                    pose, new GuiRectangle(
                            RECT_PROGRESS.x,
                            (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)),
                            RECT_PROGRESS.width,
                            (int) Math.ceil(RECT_PROGRESS.height * Math.min(v, 1))
                    ).offset(mainGui.rootElement)
            );
        }
        for (int i = 0; i < container.tile.recipesStates.size(); i++) {
            EnumAssemblyRecipeState state = new ArrayList<>(container.tile.recipesStates.values()).get(i);
            if (state == EnumAssemblyRecipeState.SAVED) {
                ICON_SAVED.drawAt(pose, getArea(i));
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
                ICON_SAVED_ENOUGH.drawAt(pose, getArea(i));
            }
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
                ICON_SAVED_ENOUGH_ACTIVE.drawAt(pose, getArea(i));
            }
        }
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        font.draw(pose, title, titleLabelX + leftPos + (imageWidth - font.width(title)) / 2, titleLabelY + topPos, 0x404040);
    }

    @Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean flag = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            for (int i = 0; i < container.tile.recipesStates.size(); i++) {
                TileAssemblyTable.AssemblyInstruction instruction = new ArrayList<>(container.tile.recipesStates.keySet()).get(i);
                EnumAssemblyRecipeState state = new ArrayList<>(container.tile.recipesStates.values()).get(i);
                if (getArea(i).contains(mouseX, mouseY)) {
                    container.tile.sendRecipeStateToServer(
                            instruction,
                            state == EnumAssemblyRecipeState.POSSIBLE
                                    ? EnumAssemblyRecipeState.SAVED
                                    : EnumAssemblyRecipeState.POSSIBLE
                    );
                    return true;
                }
            }
        }
        return flag;
    }
}
