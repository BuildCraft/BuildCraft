/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.factory.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.utils.StringUtil;
import buildcraft.factory.TileAutoWorkbench;

public class GuiAutoCrafting extends GuiBuildCraft {

	public GuiAutoCrafting(InventoryPlayer inventoryplayer, World world, TileAutoWorkbench tile) {
		super(new ContainerAutoWorkbench(inventoryplayer, tile), tile);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if (this.mc.thePlayer != null) {
			inventorySlots.onCraftGuiClosed(mc.thePlayer);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtil.localize("tile.autoWorkbenchBlock");
		fontRenderer.drawString(title, getCenteredOffset(title), 6, 0x404040);
		fontRenderer.drawString(StringUtil.localize("gui.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/gui/crafting.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

}
