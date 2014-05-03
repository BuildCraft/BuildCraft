/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngine;
import buildcraft.energy.TileEngineWithInventory;

public class GuiStoneEngine extends GuiEngine {

	private static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/steam_engine_gui.png");

	public GuiStoneEngine(InventoryPlayer inventoryplayer, TileEngineWithInventory tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine), tileEngine, TEXTURE);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		String title = StringUtils.localize("tile.engineStone.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRendererObj.drawString(StringUtils.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
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
