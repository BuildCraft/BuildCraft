/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.items.IItemHandler;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.gui.GuiBC8;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.pipe.behaviour.PipeBehaviourDiamond;

public class GuiDiamondPipe extends GuiBC8<ContainerDiamondPipe> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/filter.png");
    private static final ResourceLocation TEXTURE_CB = new ResourceLocation("buildcrafttransport:textures/gui/filter.png");
    private static final int SIZE_X = 175, SIZE_Y = 225;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_GUI_CB = new GuiIcon(TEXTURE_CB, 0, 0, SIZE_X, SIZE_Y);

    IInventory playerInventory;
    IItemHandler filterInventory;

    public GuiDiamondPipe(EntityPlayer player, PipeBehaviourDiamond pipe) {
        super(new ContainerDiamondPipe(player, pipe));
        this.playerInventory = player.inventory;
        this.filterInventory = pipe.filters;
        xSize = SIZE_X;
        ySize = SIZE_Y;
    }

    @Override
    protected void drawForegroundLayer() {
        String string = StringUtilBC.localize("gui.pipes.emerald.title");
        fontRendererObj.drawString(string, rootElement.getX() + 8, rootElement.getY() + 6, 0x404040);
        fontRendererObj.drawString(StringUtilBC.localize("gui.inventory"), rootElement.getX() + 8, rootElement.getY() + ySize - 97, 0x404040);
    }

    @Override
    protected void drawBackgroundLayer(float partialTicks) {
        if (BCCoreConfig.colourBlindMode) {
            ICON_GUI_CB.drawAt(rootElement);
        } else {
            ICON_GUI.drawAt(rootElement);
        }
    }
}
