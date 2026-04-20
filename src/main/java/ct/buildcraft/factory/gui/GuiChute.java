/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.factory.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.factory.container.ContainerChute;
import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiChute extends GuiBC8<ContainerChute> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftfactory:textures/gui/chute.png");
    private static final int SIZE_X = 176, SIZE_Y = 153;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiChute(ContainerChute container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
	protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);
    }
}
