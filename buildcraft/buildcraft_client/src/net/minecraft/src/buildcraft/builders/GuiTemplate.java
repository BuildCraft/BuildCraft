package net.minecraft.src.buildcraft.builders;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;

public class GuiTemplate extends GuiContainer {
	
	IInventory playerInventory;
	IInventory filterInventory;
	
	public GuiTemplate(IInventory playerInventory, IInventory filterInventory) {
		super(new CraftingTemplate(playerInventory, filterInventory));
		this.playerInventory = playerInventory;
		this.filterInventory = filterInventory;
		xSize = 175;
		ySize = 225;
	}
	
    protected void drawGuiContainerForegroundLayer() {
        fontRenderer.drawString(filterInventory.getInvName(), 8, 6, 0x404040);
        fontRenderer.drawString(playerInventory.getInvName(), 8, ySize - 152, 0x404040);        
    }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/builders/gui/template_gui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	int inventoryRows = 6;
}
