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
