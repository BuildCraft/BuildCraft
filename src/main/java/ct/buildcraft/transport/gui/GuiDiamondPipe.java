/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.BCLibConfig;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.transport.container.ContainerDiamondPipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.IItemHandler;

public class GuiDiamondPipe extends GuiBC8<ContainerDiamondPipe> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/filter.png");
    private static final ResourceLocation TEXTURE_CB = new ResourceLocation("buildcrafttransport:textures/gui/filter_cb.png");
    private static final int SIZE_X = 175, SIZE_Y = 225;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_GUI_CB = new GuiIcon(TEXTURE_CB, 0, 0, SIZE_X, SIZE_Y);

    Inventory playerInventory;
    IItemHandler filterInventory;

    public GuiDiamondPipe(ContainerDiamondPipe container, Inventory inv, Component title) {
        super(container, inv, title);
        this.playerInventory = inv;
        this.filterInventory = container.filters;
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        Component string = Component.translatable("gui.pipes.emerald.title");
        double titleX = mainGui.rootElement.getX() + 8;
        double titleY = mainGui.rootElement.getY() + 6;
        font.draw(pose, string, (int) titleX, (int) titleY, 0x404040);

        double invY = mainGui.rootElement.getY() + imageHeight - 97;
        font.draw(pose, Component.translatable("gui.inventory"), (int) titleX, (int) invY, 0x404040);
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        if (BCLibConfig.colourBlindMode) {
            ICON_GUI_CB.drawAt(pose, mainGui.rootElement);
        } else {
            ICON_GUI.drawAt(pose, mainGui.rootElement);
        }
    }
}
