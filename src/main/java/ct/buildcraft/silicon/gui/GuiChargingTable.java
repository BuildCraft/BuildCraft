/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.help.GuiHelpUtil;
import ct.buildcraft.silicon.container.ContainerChargingTable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiChargingTable extends GuiBC8<ContainerChargingTable> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftsilicon:textures/gui/charging_table.png");
    private static final int SIZE_X = 176;
    private static final int SIZE_Y = 132;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiChargingTable(ContainerChargingTable container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
        GuiHelpUtil.addSlot(mainGui, 80, 18, "buildcraft.help.charging_table.slot.title", 0xFF_66_AA_FF,
            "buildcraft.help.charging_table.slot.desc");
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);
    }

    @Override
    protected void drawForegroundLayer(PoseStack pose, int mouseX, int mouseY) {
        font.draw(pose, title, leftPos + (imageWidth - font.width(title)) / 2, topPos + 6, 0x404040);
        font.draw(pose, playerInventoryTitle, leftPos + 8, topPos + imageHeight - 97, 0x404040);
    }
}
