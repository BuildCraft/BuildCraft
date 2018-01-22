/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.energy.container.ContainerEngineStone_BC8;

public class GuiEngineStone_BC8 extends GuiBC8<ContainerEngineStone_BC8> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftenergy:textures/gui/steam_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    private final IGuiArea flameRect = new GuiRectangle(81, 25, 14, 14).offset(rootElement);
    private final IGuiArea fuelSlotRect = new GuiRectangle(78, 39, 20, 20).offset(rootElement);
    private final ElementHelpInfo helpFlame, helpFuel;

    public GuiEngineStone_BC8(ContainerEngineStone_BC8 container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
        shownElements.add(new LedgerEngine(this, true));
        helpFlame = new ElementHelpInfo("buildcraft.help.stone_engine.flame.title", 0xFF_FF_FF_1F, "buildcraft.help.stone_engine.flame");
        // TODO: Auto list of example fuels!
        helpFuel = new ElementHelpInfo("buildcraft.help.stone_engine.fuel.title", 0xFF_AA_33_33, "buildcraft.help.stone_engine.fuel");
    }

    @Override
    public void initGui() {
        super.initGui();
        shownElements.add(new DummyHelpElement(flameRect.expand(2), helpFlame));
        shownElements.add(new DummyHelpElement(fuelSlotRect, helpFuel));
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);

        double amount = container.tile.deltaFuelLeft.getDynamic(partialTicks) / 100;

        if (amount > 0) {
            int flameHeight = (int) Math.ceil(amount * flameRect.getHeight());

            drawTexturedModalRect(//
                    (int) flameRect.getX(),//
                    (int) (flameRect.getY() + flameRect.getHeight() - flameHeight),//
                    176, 14 - flameHeight, 14, flameHeight + 2);
        }
    }

    @Override
    protected void drawForegroundLayer() {
        String str = LocaleUtil.localize("tile.engineStone.name");
        int strWidth = fontRendererObj.getStringWidth(str);
        double titleX = rootElement.getCenterX() - strWidth / 2;
        double titleY = rootElement.getY() + 6;
        fontRendererObj.drawString(str, (int) titleX, (int) titleY, 0x404040);
        
        double invX = rootElement.getX() + 8;
        double invY = rootElement.getY() + SIZE_Y - 96;
        fontRendererObj.drawString(LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
    }
}
