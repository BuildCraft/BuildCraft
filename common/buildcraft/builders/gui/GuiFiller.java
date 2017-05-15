/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerFiller;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiButtonSmall;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.elem.ToolTip;
import net.minecraft.util.ResourceLocation;

public class GuiFiller extends GuiBC8<ContainerFiller> {
    private static final ResourceLocation TEXTURE_BASE =
            new ResourceLocation("buildcraftbuilders:textures/gui/filler.png");
    private static final int SIZE_X = 176, SIZE_Y = 241;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiFiller(ContainerFiller container) {
        super(container);
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(
                new GuiButtonSmall(
                        this,
                        0,
                        rootElement.getX() + (xSize - 100) / 2,
                        rootElement.getY() + 60,
                        100,
                        "Can Excavate"
                )
                        .setToolTip(ToolTip.createLocalized("gui.filler.canExcavate"))
                        .setBehaviour(IButtonBehaviour.TOGGLE)
                        .setActive(container.tile.canExcavate())
                        .registerListener((button, buttonId, buttonKey) ->
                                container.tile.sendCanExcavate(button.isButtonActive())
                        )
        );
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }
}
