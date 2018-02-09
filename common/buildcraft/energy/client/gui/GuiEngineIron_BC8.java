/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.energy.container.ContainerEngineIron_BC8;

public class GuiEngineIron_BC8 extends GuiBC8<ContainerEngineIron_BC8> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftenergy:textures/gui/combustion_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 60);

    public GuiEngineIron_BC8(ContainerEngineIron_BC8 container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
    }

    @Override
    public void initGui() {
        super.initGui();

        mainGui.shownElements.add(container.widgetTankFuel.createGuiElement(mainGui, new GuiRectangle(26, 18, 16, 60).offset(mainGui.rootElement), ICON_TANK_OVERLAY));
        mainGui.shownElements.add(container.widgetTankCoolant.createGuiElement(mainGui, new GuiRectangle(80, 18, 16, 60).offset(mainGui.rootElement), ICON_TANK_OVERLAY));
        mainGui.shownElements.add(container.widgetTankResidue.createGuiElement(mainGui, new GuiRectangle(134, 18, 16, 60).offset(mainGui.rootElement), ICON_TANK_OVERLAY));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(mainGui.rootElement);
    }

    @Override
    protected void drawForegroundLayer() {
        String str = LocaleUtil.localize("tile.engineIron.name");
        int strWidth = fontRenderer.getStringWidth(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2;
        double titleY = mainGui.rootElement.getY() + 6;
        fontRenderer.drawString(str, (int) titleX, (int) titleY, 0x404040);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
    }
}
