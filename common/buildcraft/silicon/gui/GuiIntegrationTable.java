/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.silicon.gui;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.silicon.container.ContainerIntegrationTable;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Point2i;

public class GuiIntegrationTable extends GuiBC8<ContainerIntegrationTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/integration_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 22, 4, 70);

    public GuiIntegrationTable(ContainerIntegrationTable container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    private Point2i getPos(int index) {
        int posX = index % 3;
        int posY = index / 3;
        return new Point2i(116 + posX * 18, 36 + posY * 18);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        long target = container.tile.getTarget();
        if(target != 0) {
            double v = (double) container.tile.power / target;
            ICON_PROGRESS.drawCutInside(
                    new GuiRectangle(
                            RECT_PROGRESS.x,
                            (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)),
                            RECT_PROGRESS.width,
                            (int) Math.ceil(RECT_PROGRESS.height * Math.min(v, 1))
                    ).offset(rootElement)
            );
        }

        if(container.tile.recipe != null) {
            drawItemStackAt(container.tile.recipe.output, rootElement.getX() + 101, rootElement.getY() + 36);
        }
    }

    @Override
    protected void drawForegroundLayer() {
        String title = I18n.format("tile.integrationTableBlock.name");
        fontRendererObj.drawString(title, guiLeft + (xSize - fontRendererObj.getStringWidth(title)) / 2, guiTop + 10, 0x404040);
    }
}
