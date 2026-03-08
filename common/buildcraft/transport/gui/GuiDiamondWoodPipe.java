/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.gui;

import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiImageButton;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.button.IButtonClickEventTrigger;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiDiamondWoodPipe extends GuiBC8<ContainerDiamondWoodPipe> implements IButtonClickEventListener {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/pipe_emerald.png");
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("buildcrafttransport:textures/gui/pipe_emerald_button.png");
    private static final int WHITE_LIST_BUTTON_ID = FilterMode.WHITE_LIST.ordinal();
    private static final int BLACK_LIST_BUTTON_ID = FilterMode.BLACK_LIST.ordinal();
    private static final int ROUND_ROBIN_BUTTON_ID = FilterMode.ROUND_ROBIN.ordinal();
    private static final int SIZE_X = 175, SIZE_Y = 161;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_ROUND_ROBIN_INDEX = new GuiIcon(TEXTURE, 176, 0, 20, 20);
    private static final GuiIcon ICON_ROUND_ROBIN_NONE = new GuiIcon(TEXTURE, 176, 20, 20, 20);

    private GuiImageButton whiteListButton;
    private GuiImageButton blackListButton;
    private GuiImageButton roundRobinButton;

    private PipeBehaviourWoodDiamond pipe;

    // public GuiDiamondWoodPipe(PlayerEntity player, PipeBehaviourWoodDiamond pipe, Inventory inventory, ITextComponent component)
    public GuiDiamondWoodPipe(ContainerDiamondWoodPipe container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);

//        this.pipe = pipe;
        this.pipe = ((PipeBehaviourWoodDiamond) container.pipeHolder.getPipe().getBehaviour());

//        xSize = SIZE_X;
        imageWidth = SIZE_X;
//        ySize = SIZE_Y;
        imageHeight = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.whiteListButton = new GuiImageButton(mainGui, WHITE_LIST_BUTTON_ID, this.leftPos + 7, this.topPos + 41, 18, TEXTURE_BUTTON, 19, 19);
        this.whiteListButton.setToolTip(ToolTip.createLocalized("tip.PipeItemsEmerald.whitelist"));
        this.whiteListButton.registerListener(this);
        this.mainGui.shownElements.add(this.whiteListButton);

        this.blackListButton = new GuiImageButton(mainGui, BLACK_LIST_BUTTON_ID, this.leftPos + 7 + 18, this.topPos + 41, 18, TEXTURE_BUTTON, 37, 19);
        this.blackListButton.setToolTip(ToolTip.createLocalized("tip.PipeItemsEmerald.blacklist"));
        this.blackListButton.registerListener(this);
        this.mainGui.shownElements.add(this.blackListButton);

        if (pipe.pipe.getFlow() instanceof IFlowItems) {
            // Don't show round robin for the fluid pipe - its not yet implemented
            this.roundRobinButton = new GuiImageButton(mainGui, ROUND_ROBIN_BUTTON_ID, this.leftPos + 7 + 36, this.topPos + 41, 18, TEXTURE_BUTTON, 55, 19);
            this.roundRobinButton.setToolTip(ToolTip.createLocalized("tip.PipeItemsEmerald.roundrobin"));
            this.roundRobinButton.registerListener(this);
            this.mainGui.shownElements.add(this.roundRobinButton);
            IButtonBehaviour.createAndSetRadioButtons(whiteListButton, blackListButton, roundRobinButton);
        } else {
            IButtonBehaviour.createAndSetRadioButtons(whiteListButton, blackListButton);
        }

        switch (pipe.filterMode) {
            case WHITE_LIST:
                this.whiteListButton.activate();
                break;
            case BLACK_LIST:
                this.blackListButton.activate();
                break;
            case ROUND_ROBIN:
                if (roundRobinButton != null) {
                    this.roundRobinButton.activate();
                }
                break;
        }
    }

    @Override
    public void handleButtonClick(IButtonClickEventTrigger sender, int buttonKey) {
        if (!(sender instanceof GuiImageButton)) {
            return;
        }
        int id = Integer.parseInt(((GuiImageButton) sender).id);
        FilterMode newFilterMode = FilterMode.get(id);
        this.pipe.filterMode = newFilterMode;
        container.sendNewFilterMode(newFilterMode);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks, MatrixStack poseStack) {
        ICON_GUI.drawAt(mainGui.rootElement, poseStack);
    }

    @Override
    protected void drawForegroundLayer(MatrixStack poseStack) {
        String title = LocaleUtil.localize("gui.pipes.emerald.title");
//        double titleX = mainGui.rootElement.getX() + (xSize - fontRenderer.getStringWidth(title)) / 2;
        double titleX = mainGui.rootElement.getX() + (imageWidth - font.width(title)) / 2;
//        fontRenderer.drawString(title, (int) titleX, (int) mainGui.rootElement.getY() + 6, 0x404040);
        font.draw(poseStack, title, (int) titleX, (int) mainGui.rootElement.getY() + 6, 0x404040);
//        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), (int) mainGui.rootElement.getX() + 8, (int) mainGui.rootElement.getY() + ySize - 93, 0x404040);
        font.draw(poseStack, LocaleUtil.localize("gui.inventory"), (int) mainGui.rootElement.getX() + 8, (int) mainGui.rootElement.getY() + imageHeight - 93, 0x404040);
        if (pipe.filterMode == FilterMode.ROUND_ROBIN) {
//            GlStateManager.color(1, 1, 1, 1);
            RenderUtil.color(1, 1, 1, 1);
            GuiIcon icon = pipe.filterValid ? ICON_ROUND_ROBIN_INDEX : ICON_ROUND_ROBIN_NONE;
            int x = pipe.filterValid ? 18 * pipe.currentFilter : 0;
            icon.drawAt(poseStack, mainGui.rootElement.getX() + 6 + x, mainGui.rootElement.getY() + 16);
        }
    }
}
