/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.energy.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.utils.BCStringUtils;
import buildcraft.energy.TileEngineIron;

public class GuiCombustionEngine extends GuiEngine {

    private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraftenergy:textures/gui/combustion_engine_gui.png");

    public GuiCombustionEngine(EntityPlayer player, TileEngineIron tileEngine) {
        super(new ContainerEngine(player, tileEngine), tileEngine, TEXTURE);
        ySize = 177;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);
        String title = BCStringUtils.localize("tile.engineIron.name");
        fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
        fontRendererObj.drawString(BCStringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
    }
}
