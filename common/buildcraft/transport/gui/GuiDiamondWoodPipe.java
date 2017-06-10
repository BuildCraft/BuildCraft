/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.transport.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.transport.pipe.IFlowItems;

import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.button.GuiImageButton;
import buildcraft.lib.gui.button.IButtonBehaviour;
import buildcraft.lib.gui.button.IButtonClickEventListener;
import buildcraft.lib.gui.button.IButtonClickEventTrigger;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.misc.LocaleUtil;

import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;

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

    public GuiDiamondWoodPipe(EntityPlayer player, PipeBehaviourWoodDiamond pipe) {
        super(new ContainerDiamondWoodPipe(player, pipe));

        this.pipe = pipe;

        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.whiteListButton = new GuiImageButton(this, WHITE_LIST_BUTTON_ID, this.guiLeft + 7, this.guiTop + 41, 18, TEXTURE_BUTTON, 19, 19);
        this.whiteListButton.setToolTip(ToolTip.createLocalized("tip.PipeItemsEmerald.whitelist"));
        this.whiteListButton.registerListener(this);
        this.guiElements.add(this.whiteListButton);

        this.blackListButton = new GuiImageButton(this, BLACK_LIST_BUTTON_ID, this.guiLeft + 7 + 18, this.guiTop + 41, 18, TEXTURE_BUTTON, 37, 19);
        this.blackListButton.setToolTip(ToolTip.createLocalized("tip.PipeItemsEmerald.blacklist"));
        this.blackListButton.registerListener(this);
        this.guiElements.add(this.blackListButton);

        if (pipe.pipe.getFlow() instanceof IFlowItems) {
            // Don't show round robin for the fluid pipe - its not yet implemented
            this.roundRobinButton = new GuiImageButton(this, ROUND_ROBIN_BUTTON_ID, this.guiLeft + 7 + 36, this.guiTop + 41, 18, TEXTURE_BUTTON, 55, 19);
            this.roundRobinButton.setToolTip(ToolTip.createLocalized("tip.PipeItemsEmerald.roundrobin"));
            this.roundRobinButton.registerListener(this);
            this.guiElements.add(this.roundRobinButton);
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
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }

    @Override
    protected void drawForegroundLayer() {
        String title = LocaleUtil.localize("gui.pipes.emerald.title");
        fontRenderer.drawString(title, rootElement.getX() + (xSize - fontRenderer.getStringWidth(title)) / 2, rootElement.getY() + 6, 0x404040);
        fontRenderer.drawString(LocaleUtil.localize("gui.inventory"), rootElement.getX() + 8, rootElement.getY() + ySize - 93, 0x404040);
        if (pipe.filterMode == FilterMode.ROUND_ROBIN) {
            GlStateManager.color(1, 1, 1, 1);
            GuiIcon icon = pipe.filterValid ? ICON_ROUND_ROBIN_INDEX : ICON_ROUND_ROBIN_NONE;
            int x = pipe.filterValid ? 18 * pipe.currentFilter : 0;
            icon.drawAt(rootElement.getX() + 6 + x, rootElement.getY() + 16);
        }
    }
}
