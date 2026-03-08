/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandler;

public class GuiDiamondPipe extends GuiBC8<ContainerDiamondPipe> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/filter.png");
    private static final ResourceLocation TEXTURE_CB = new ResourceLocation("buildcrafttransport:textures/gui/filter_cb.png");
    private static final int SIZE_X = 175, SIZE_Y = 225;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_GUI_CB = new GuiIcon(TEXTURE_CB, 0, 0, SIZE_X, SIZE_Y);

    // IInventory playerInventory;
    IInventory playerInventory;
    IItemHandler filterInventory;

    // public GuiDiamondPipe(PlayerEntity player, PipeBehaviourDiamond pipe, Inventory inventory, ITextComponent component)
    public GuiDiamondPipe(ContainerDiamondPipe container, PlayerInventory inventory, ITextComponent component) {
//        super(new ContainerDiamondPipe(null, -1, player, pipe), inventory, component);
        super(container, inventory, component);
//        this.playerInventory = player.inventory;
//        this.playerInventory = player.getInventory();
        this.playerInventory = inventory;
//        this.filterInventory = pipe.filters;
        this.filterInventory = ((PipeBehaviourDiamond) container.pipeHolder.getPipe().getBehaviour()).filters;
//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
    }

    @Override
//    protected void drawForegroundLayer()
    protected void drawForegroundLayer(MatrixStack poseStack) {
        String string = LocaleUtil.localize("gui.pipes.emerald.title");
        double titleX = mainGui.rootElement.getX() + 8;
        double titleY = mainGui.rootElement.getY() + 6;
//        fontRenderer.drawString(string, (int) titleX, (int) titleY, 0x404040);
        font.draw(poseStack, string, (int) titleX, (int) titleY, 0x404040);

//        double invY = mainGui.rootElement.getY() + ySize - 97;
        double invY = mainGui.rootElement.getY() + imageHeight - 97;
//        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), (int) titleX, (int) invY, 0x404040);
        font.draw(poseStack, LocaleUtil.localize("gui.inventory"), (int) titleX, (int) invY, 0x404040);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, MatrixStack poseStack) {
        if (BCLibConfig.colourBlindMode) {
            ICON_GUI_CB.drawAt(mainGui.rootElement, poseStack);
        } else {
            ICON_GUI.drawAt(mainGui.rootElement, poseStack);
        }
    }
}
