/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.World;

public class GuiAutoCrafting extends GuiContainer {

	public GuiAutoCrafting(InventoryPlayer inventoryplayer, World world,
			TileAutoWorkbench tile) {
		super(new ContainerAutoWorkbench(inventoryplayer, world, tile));
	}

	public void onGuiClosed() {
		super.onGuiClosed();
		inventorySlots.onCraftGuiClosed(mc.thePlayer);
	}

	protected void drawGuiContainerForegroundLayer() {
		fontRenderer.drawString("Crafting", 28, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
	}

	protected void drawGuiContainerBackgroundLayer(float f) {
		int i = mc.renderEngine.getTexture("/gui/crafting.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

}
