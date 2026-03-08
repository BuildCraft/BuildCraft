/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.client.gui;

import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.container.ContainerEngineStone_BC8;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.help.DummyHelpElement;
import buildcraft.lib.gui.help.ElementHelpInfo;
import buildcraft.lib.gui.ledger.LedgerEngine;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.gui.pos.IGuiArea;
import buildcraft.lib.misc.LocaleUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiEngineStone_BC8 extends GuiBC8<ContainerEngineStone_BC8> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftenergy:textures/gui/steam_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    private final IGuiArea flameRect = new GuiRectangle(81, 25, 14, 14).offset(mainGui.rootElement);
    private final IGuiArea fuelSlotRect = new GuiRectangle(78, 39, 20, 20).offset(mainGui.rootElement);
    private final ElementHelpInfo helpFlame, helpFuel;

    public GuiEngineStone_BC8(ContainerEngineStone_BC8 container, Inventory inventory, Component component) {
        super(container, inventory, component);
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
        helpFlame = new ElementHelpInfo("buildcraft.help.stone_engine.flame.title", 0xFF_FF_FF_1F, "buildcraft.help.stone_engine.flame");
        // TODO: Auto list of example fuels!
        helpFuel = new ElementHelpInfo("buildcraft.help.stone_engine.fuel.title", 0xFF_AA_33_33, "buildcraft.help.stone_engine.fuel");
    }

    @Override
    public void initGui() {
        mainGui.shownElements.add(new DummyHelpElement(flameRect.expand(2), helpFlame));
        mainGui.shownElements.add(new DummyHelpElement(fuelSlotRect, helpFuel));
    }

    @Override
//    protected void drawBackgroundLayer(float partialTicks)
    protected void drawBackgroundLayer(float partialTicks, PoseStack poseStack) {
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);

        double amount = container.tile.deltaFuelLeft.getDynamic(partialTicks) / 100;

        if (amount > 0) {
            int flameHeight = (int) Math.ceil(amount * flameRect.getHeight());

//            drawTexturedModalRect(//
//                    poseStack,
//                    (int) flameRect.getX(),//
//                    (int) (flameRect.getY() + flameRect.getHeight() - flameHeight),//
//                    176, 14 - flameHeight, 14, flameHeight + 2
//            );
            blit(//
                    poseStack,
                    (int) flameRect.getX(),//
                    (int) (flameRect.getY() + flameRect.getHeight() - flameHeight),//
                    176, 14 - flameHeight, 14, flameHeight + 2
            );
        }
    }

    @Override
//    protected void drawForegroundLayer()
    protected void drawForegroundLayer(PoseStack poseStack) {
//        String str = LocaleUtil.localize("tile.engineStone.name");
        String str = LocaleUtil.localize(BCEnergyBlocks.engineStone.get().getDescriptionId());
//        int strWidth = fontRenderer.getStringWidth(str);
        int strWidth = font.width(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2;
        double titleY = mainGui.rootElement.getY() + 6;
//        fontRenderer.drawString(str, (int) titleX, (int) titleY, 0x404040);
        font.draw(poseStack, str, (int) titleX, (int) titleY, 0x404040);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
//        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
        font.draw(poseStack, LocaleUtil.localize("gui.inventory"), (int) invX, (int) invY, 0x404040);
    }
}
