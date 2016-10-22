/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import buildcraft.BuildCraftCore;
import buildcraft.core.lib.gui.GuiBuildCraft;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.IDiamondPipe;

public class GuiDiamondPipe extends GuiBuildCraft {

    private static final ResourceLocation TEXTURE;
    IInventory playerInventory;
    IInventory filterInventory;

    static {
        // FIXME: Make this listen to the property update in-game
        if (!BuildCraftCore.colorBlindMode) {
            TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/filter.png");
        } else {
            TEXTURE = new ResourceLocation("buildcrafttransport:textures/gui/filter_cb.png");
        }
    }

    public GuiDiamondPipe(EntityPlayer player, IDiamondPipe pipe) {
        super(new ContainerDiamondPipe(player, pipe), pipe.getFilters(), TEXTURE);
        this.playerInventory = player.inventory;
        this.filterInventory = pipe.getFilters();
        xSize = 175;
        ySize = 225;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        String string = filterInventory.getDisplayName().getFormattedText();
        fontRendererObj.drawString(string, getCenteredOffset(string), 6, 0x404040);
        fontRendererObj.drawString(StringUtilBC.localize("gui.inventory"), 8, ySize - 97, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}
