/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package ct.buildcraft.silicon.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.pos.GuiRectangle;
import ct.buildcraft.silicon.container.ContainerIntegrationTable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import ct.buildcraft.lib.gui.help.GuiHelpUtil;

public class GuiIntegrationTable extends GuiBC8<ContainerIntegrationTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/integration_table.png");
    private static final int SIZE_X = 176;
    private static final int SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 176, 0, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 22, 4, 70);

    public GuiIntegrationTable(ContainerIntegrationTable container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
        // Match the actual slot positions from ContainerIntegrationTable. The integration input slots are arranged
        // as a 3x3 ring around the centre target slot, so a simple rectangular 2x3 highlight is visibly wrong.
        GuiHelpUtil.addMenuSlot(mainGui, container.slots, 0, "buildcraft.help.integration.target.title", 0xFF_66_AA_FF,
            "buildcraft.help.integration.target.desc");
        GuiHelpUtil.addMenuSlots(mainGui, container.slots, 1, 9, "buildcraft.help.integration.ingredients.title", 0xFF_88_CC_88,
            "buildcraft.help.integration.ingredients.desc");
        GuiHelpUtil.addMenuSlot(mainGui, container.slots, 9, "buildcraft.help.integration.preview.title", 0xFF_DD_CC_55,
            "buildcraft.help.integration.preview.desc");
        GuiHelpUtil.addMenuSlot(mainGui, container.slots, 10, "buildcraft.help.integration.result.title", 0xFF_CC_AA_FF,
            "buildcraft.help.integration.result.desc");
        GuiHelpUtil.addRoot(mainGui, 164, 22, 4, 70, "buildcraft.help.silicon.progress.title", 0xFF_DD_CC_55,
            "buildcraft.help.silicon.progress.desc");
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);

        long target = container.tile.getGuiTarget();
        if (target != 0) {
            double v = (double) container.tile.power / target;
            ICON_PROGRESS.drawCutInside(
                    pose, new GuiRectangle(
                            RECT_PROGRESS.x,
                            (int) (RECT_PROGRESS.y + RECT_PROGRESS.height * Math.max(1 - v, 0)),
                            RECT_PROGRESS.width,
                            (int) Math.ceil(RECT_PROGRESS.height * Math.min(v, 1))
                    ).offset(mainGui.rootElement)
            );
        }
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        font.draw(pose, title, leftPos + (imageWidth - font.width(title)) / 2, topPos + 6, 0x404040);
    }
}
