/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngineIron;

public class GuiCombustionEngine extends GuiEngine {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/combustion_engine_gui.png");

	public GuiCombustionEngine(InventoryPlayer inventoryplayer, TileEngineIron tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine), tileEngine, TEXTURE);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.engineIron.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		TileEngineIron engine = (TileEngineIron) tile;
        drawFluid(engine.getFuel(), j + 104, k + 19, 16, 58, TileEngineIron.MAX_LIQUID);
        drawFluid(engine.getCoolant(), j + 122, k + 19, 16, 58, TileEngineIron.MAX_LIQUID);
        mc.renderEngine.bindTexture(TEXTURE);
        drawTexturedModalRect(j + 104, k + 19, 176, 0, 16, 60);
        drawTexturedModalRect(j + 122, k + 19, 176, 0, 16, 60);
	}
}
