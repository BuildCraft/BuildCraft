/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy.gui;

import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtil;
import buildcraft.energy.TileEngine;

public class GuiSteamEngine extends GuiEngine {

	public GuiSteamEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine), tileEngine);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtil.localize("tile.engineStone");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture(DefaultProps.TEXTURE_PATH_GUI + "/steam_engine_gui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		TileEngine engine = (TileEngine) tile;
		if (engine.getScaledBurnTime(12) > 0) {
			int l = engine.getScaledBurnTime(12);

			drawTexturedModalRect(j + 80, (k + 24 + 12) - l, 176, 12 - l, 14, l + 2);
		}
	}
}
