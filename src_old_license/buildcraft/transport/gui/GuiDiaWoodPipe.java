/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.gui.buttons.GuiImageButton;
import buildcraft.core.lib.gui.buttons.IButtonClickEventListener;
import buildcraft.core.lib.gui.buttons.IButtonClickEventTrigger;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.elem.ToolTip;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.container.ContainerDiaWoodPipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiaWood;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiaWood.FilterMode;

public class GuiDiaWoodPipe extends GuiBC8<ContainerDiaWoodPipe> implements IButtonClickEventListener {

    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/pipe_emerald.png");
    private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("buildcrafttransport:textures/gui/pipe_emerald_button.png");
    private static final int WHITE_LIST_BUTTON_ID = 1;
    private static final int BLACK_LIST_BUTTON_ID = 2;
    private static final int ROUND_ROBIN_BUTTON_ID = 3;
    private static final int SIZE_X = 175, SIZE_Y = 161;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);

    private GuiImageButton whiteListButton;
    private GuiImageButton blackListButton;
    private GuiImageButton roundRobinButton;

    private PipeBehaviourDiaWood pipe;

    public GuiDiaWoodPipe(EntityPlayer player, PipeBehaviourDiaWood pipe) {
        super(new ContainerDiaWoodPipe(player, pipe));

        this.pipe = pipe;

        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();

        this.whiteListButton = new GuiImageButton(this, WHITE_LIST_BUTTON_ID, this.guiLeft + 7, this.guiTop + 41, 18, TEXTURE_BUTTON, 19, 19);
        this.whiteListButton.registerListener(this);
        this.whiteListButton.setToolTip(new ToolTip(500, StringUtilBC.localize("tip.PipeItemsEmerald.whitelist")));
        this.buttonList.add(this.whiteListButton);

        this.blackListButton = new GuiImageButton(this, BLACK_LIST_BUTTON_ID, this.guiLeft + 7 + 18, this.guiTop + 41, 18, TEXTURE_BUTTON, 37, 19);
        this.blackListButton.registerListener(this);
        this.blackListButton.setToolTip(new ToolTip(500, StringUtilBC.localize("tip.PipeItemsEmerald.blacklist")));
        this.buttonList.add(this.blackListButton);

        this.roundRobinButton = new GuiImageButton(this, ROUND_ROBIN_BUTTON_ID, this.guiLeft + 7 + 36, this.guiTop + 41, 18, TEXTURE_BUTTON, 55, 19);
        this.roundRobinButton.registerListener(this);
        this.roundRobinButton.setToolTip(new ToolTip(500, StringUtilBC.localize("tip.PipeItemsEmerald.roundrobin")));
        this.buttonList.add(this.roundRobinButton);

        switch (pipe.filterMode) {
            case WHITE_LIST:
                this.whiteListButton.activate();
                break;
            case BLACK_LIST:
                this.blackListButton.activate();
                break;
            case ROUND_ROBIN:
                this.roundRobinButton.activate();
                break;
        }
    }

    @Override
    public void handleButtonClick(IButtonClickEventTrigger sender, int buttonId) {
        FilterMode newFilterMode = pipe.filterMode;

        switch (buttonId) {
            case WHITE_LIST_BUTTON_ID:
                whiteListButton.activate();
                blackListButton.deActivate();
                roundRobinButton.deActivate();

                newFilterMode = FilterMode.WHITE_LIST;
                break;
            case BLACK_LIST_BUTTON_ID:
                whiteListButton.deActivate();
                blackListButton.activate();
                roundRobinButton.deActivate();

                newFilterMode = FilterMode.BLACK_LIST;
                break;
            case ROUND_ROBIN_BUTTON_ID:
                whiteListButton.deActivate();
                blackListButton.deActivate();
                roundRobinButton.activate();

                newFilterMode = FilterMode.ROUND_ROBIN;
                break;
        }
    }

    @Override
    protected void drawForegroundLayer() {
        String title = StringUtilBC.localize("gui.pipes.emerald.title");
        fontRendererObj.drawString(title, rootElement.getX() + (xSize - fontRendererObj.getStringWidth(title)) / 2, rootElement.getY() + 6, 0x404040);
        fontRendererObj.drawString(StringUtilBC.localize("gui.inventory"), rootElement.getX() + 8, rootElement.getY() + ySize - 93, 0x404040);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        ICON_GUI.drawAt(rootElement);
    }
}
