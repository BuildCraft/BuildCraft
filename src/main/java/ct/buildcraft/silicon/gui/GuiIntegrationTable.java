/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.silicon.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.silicon.container.ContainerIntegrationTable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiIntegrationTable extends GuiBC8<ContainerIntegrationTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/integration_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 22, 4, 70);

    public GuiIntegrationTable(ContainerIntegrationTable container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);

        long target = container.tile.getTarget();
        if(target != 0) {
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
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        MutableComponent title = Component.translatable("tile.integrationTableBlock.name");
        font.draw(pose, title, titleLabelX + (imageWidth - font.width(title)) / 2, titleLabelY + 10, 0x404040);
    }
}
