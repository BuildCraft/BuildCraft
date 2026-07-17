/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiImageButton;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.gui.pos.GuiRectangle;

import buildcraft.builders.container.ContainerBuilder;

// TODO: Convert this gui into JSON!
public class GuiBuilder extends GuiBC8<ContainerBuilder> {
    private static final ResourceLocation TEXTURE_BASE =
            new ResourceLocation("buildcraftbuilders:textures/gui/builder.png");
    private static final ResourceLocation TEXTURE_BLUEPRINT =
            new ResourceLocation("buildcraftbuilders:textures/gui/builder_blueprint.png");
    private static final int SIZE_X = 176, SIZE_BLUEPRINT_X = 256, SIZE_Y = 222, BLUEPRINT_WIDTH = 87;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_BLUEPRINT_GUI = new GuiIcon(
            TEXTURE_BLUEPRINT,
            SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH,
            0,
            BLUEPRINT_WIDTH,
            SIZE_Y
    );
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BLUEPRINT, 0, 54, 16, 47);

    public GuiBuilder(ContainerBuilder container) {
        super(container);
        xSize = SIZE_BLUEPRINT_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int i = 0; i < container.widgetTanks.size(); i++) {
            mainGui.shownElements.add(
                    container.widgetTanks
                    .get(i).createGuiElement(mainGui, new GuiRectangle(179 + i * 18, 145, 16, 47).offset(mainGui.rootElement), ICON_TANK_OVERLAY)
            );
        }

        // "Don't excavate" (canExcavate) toggle.
        // The logic already exists on TileBuilder; this just exposes it in the GUI.
        // Reuses the shared toggle frames from buildcraftcore's list_new.png so no new texture asset is needed.
        GuiImageButton buttonExcavate = new GuiImageButton(
                mainGui,
                0,
                (int) (mainGui.rootElement.getX() + 161),
                (int) (mainGui.rootElement.getY() + 8),
                11,
                new ResourceLocation("buildcraftcore:textures/gui/list_new.png"),
                176, 16, 176, 28);
        buttonExcavate.setToolTip(ToolTip.createLocalized("gui.builder.canExcavate"));
        buttonExcavate.setBehaviour(IButtonBehaviour.TOGGLE);
        buttonExcavate.setActive(container.tile.canExcavate());
        buttonExcavate.registerListener((button, buttonKey) ->
                container.tile.sendCanExcavate(button.isButtonActive()));
        mainGui.shownElements.add(buttonExcavate);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(mainGui.rootElement);
        ICON_BLUEPRINT_GUI.drawAt(mainGui.rootElement.offset(SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH, 0));
    }
}
