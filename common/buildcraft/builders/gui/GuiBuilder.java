/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;

import buildcraft.builders.container.ContainerBuilder;

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

//        ledgersRight.ledgers.add(new LedgerCounters(ledgersRight, container.tile));
    }

    @Override
    public void initGui() {
        super.initGui();

        for (int i = 0; i < container.widgetTanks.size(); i++) {
            shownElements.add(
                    container.widgetTanks
                    .get(i).createGuiElement(this, new GuiRectangle(179 + i * 18, 145, 16, 47).offset(rootElement), ICON_TANK_OVERLAY)
            );
        }

//        buttonList.add(
//                new GuiButtonSmall(
//                        this,
//                        0,
//                        rootElement.getX() + (ICON_GUI.width - 100) / 2,
//                        rootElement.getY() + 50,
//                        100,
//                        "Can Excavate"
//                )
//                        .setToolTip(ToolTip.createLocalized("gui.builder.canExcavate"))
//                        .setBehaviour(IButtonBehaviour.TOGGLE)
//                        .setActive(container.tile.canExcavate())
//                        .registerListener((button, buttonId, buttonKey) ->
//                                container.tile.sendCanExcavate(button.isButtonActive())
//                        )
//        );
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
        ICON_BLUEPRINT_GUI.drawAt(rootElement.offset(SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH, 0));
    }
}
