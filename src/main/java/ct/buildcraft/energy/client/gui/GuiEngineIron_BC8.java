/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.energy.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.energy.menu.ContainerEngineIron_BC8;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.ledger.LedgerEngine;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiEngineIron_BC8 extends GuiBC8<ContainerEngineIron_BC8> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftenergy:textures/gui/combustion_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 60);

    public GuiEngineIron_BC8(ContainerEngineIron_BC8 container, Inventory inv, Component title) {
        super(container, inv, title == Component.empty() ? 
        		Component.translatable("tile.engineIron.name").withStyle(Style.EMPTY.withColor(0x404040))
        		: title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        inventoryLabelX = 8;
        inventoryLabelY = SIZE_Y - 96;
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
    }

    @Override
    public void init() {
        super.init();

        mainGui.shownElements.add(container.widgetTankFuel.createGuiElement(mainGui, new GuiRectangle(26, 18, 16, 60).offset(mainGui.rootElement), ICON_TANK_OVERLAY));
        mainGui.shownElements.add(container.widgetTankCoolant.createGuiElement(mainGui, new GuiRectangle(80, 18, 16, 60).offset(mainGui.rootElement), ICON_TANK_OVERLAY));
        mainGui.shownElements.add(container.widgetTankResidue.createGuiElement(mainGui, new GuiRectangle(134, 18, 16, 60).offset(mainGui.rootElement), ICON_TANK_OVERLAY));
    }

    @Override
	protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);
    }
}
