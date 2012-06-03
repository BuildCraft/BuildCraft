/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.buildcraft.core.GuiBuildCraft;
import net.minecraft.src.buildcraft.core.utils.StringUtil;

import org.lwjgl.opengl.GL11;

public class GuiSteamEngine extends GuiEngine {

	public GuiSteamEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine));
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
		String title = StringUtil.localize("tile.engineStone");
        fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/energy/gui/steam_engine_gui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		TileEngine engine = (TileEngine)tile;
		if (engine.getScaledBurnTime(12) > 0) {
			int l = engine.getScaledBurnTime(12);

			drawTexturedModalRect(j + 80, (k + 24 + 12) - l, 176, 12 - l, 14,
					l + 2);
		}
	}
}
