/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiSteamEngine extends GuiContainer {

    private TileEngine tileEngine;

	public GuiSteamEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine) {
		super(new ContainerEngine(inventoryplayer, tileEngine));
		this.tileEngine = tileEngine;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Steam Engine", 60, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/energy/gui/steam_engine_gui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		if (tileEngine.getScaledBurnTime(12) > 0) {
			int l = tileEngine.getScaledBurnTime(12);

			drawTexturedModalRect(j + 80, (k + 24 + 12) - l, 176, 12 - l, 14,
					l + 2);
		}
	}
}
