/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.gui;

import net.minecraft.util.Identifier;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.factory.container.ContainerTank;

public class GuiTank extends GuiBC8<ContainerTank> {
    private static final Identifier TEXTURE_BASE = new Identifier("buildcraftfactory:textures/gui/tank.png");
    private static final int SIZE_X = 176, SIZE_Y = 181;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 64);

    public GuiTank(ContainerTank container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;

        mainGui.shownElements.add(container.widgetTank.createGuiElement(mainGui, new GuiRectangle(80, 18, 16, 64).offset(mainGui.rootElement), ICON_TANK_OVERLAY));
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(mainGui.rootElement);
    }

    // @Override -- removed: method does not exist in Fabric 1.20.1
    protected void drawForegroundLayer() {
        String str = LocaleUtil.localize("tile.tankBlock.name");
        int strWidth = fontRenderer.getWidth(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2;
        double titleY = mainGui.rootElement.getY() + 6;
        // TODO(migration): fontRenderer.draw in 1.20.1 needs DrawContext; originally: draw(str, (int) titleX, (int) titleY, 0x404040)

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        // TODO(migration): fontRenderer.draw in 1.20.1 needs DrawContext; originally: draw(LocaleUtil.localize("gui.getInventory()"), (int) invX, (int) invY, 0x404040)
    }
}
