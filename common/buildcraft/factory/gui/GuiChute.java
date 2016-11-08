/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.factory.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.factory.container.ContainerChute;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;

public class GuiChute extends GuiBC8<ContainerChute> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftfactory:textures/gui/chute.png");
    private static final int SIZE_X = 176, SIZE_Y = 153;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiChute(ContainerChute container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }
}
