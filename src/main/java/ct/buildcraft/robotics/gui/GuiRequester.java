/* Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 */
package ct.buildcraft.robotics.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.gui.GuiBC8;
import ct.buildcraft.lib.gui.GuiIcon;
import ct.buildcraft.lib.gui.help.GuiHelpUtil;
import ct.buildcraft.robotics.container.ContainerRequester;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class GuiRequester extends GuiBC8<ContainerRequester> {
    private static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraftrobotics:textures/gui/requester_gui.png");
    private static final int SIZE_X = 196;
    private static final int SIZE_Y = 181;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiRequester(ContainerRequester container, Inventory inv, Component title) {
        super(container, inv, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;

        GuiHelpUtil.addSlots(mainGui, 9, 7, 4, 5,
                "buildcraftrobotics.help.requester.templates.title",
                0xFF_DD_88_88,
                "buildcraftrobotics.help.requester.templates.desc");
        GuiHelpUtil.addSlots(mainGui, 117, 7, 4, 5,
                "buildcraftrobotics.help.requester.storage.title",
                0xFF_88_CC_88,
                "buildcraftrobotics.help.requester.storage.desc");
        GuiHelpUtil.addRoot(mainGui, 87, 44, 22, 15,
                "buildcraftrobotics.help.requester.arrow.title",
                0xFF_CC_CC_CC,
                "buildcraftrobotics.help.requester.arrow.desc");
    }

    @Override
    protected void drawBackgroundLayer(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        ICON_GUI.drawAt(pose, mainGui.rootElement);
    }
}
