/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.gui;

import buildcraft.builders.container.ContainerBuilder;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

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

    public GuiBuilder(ContainerBuilder container, Inventory inventory, Component component) {
        super(container, inventory, component);
//        xSize = SIZE_BLUEPRINT_X;
        imageWidth = SIZE_BLUEPRINT_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
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

        // here is comment in 1.12.2
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
    protected void drawBackgroundLayer(float partialTicks, PoseStack poseStack) {
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);
        ICON_BLUEPRINT_GUI.drawAt(mainGui.rootElement.offset(SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH, 0), poseStack);
    }
}
