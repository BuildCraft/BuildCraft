package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiCombustionEngine extends GuiContainer {

    private TileEngine tileEngine;

	public GuiCombustionEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine)
    {
        super(new ContainerCombustionEngine(inventoryplayer, tileEngine));
        this.tileEngine = tileEngine;
    }

    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Combustion Engine", 50, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f)
    {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/energy/gui/combustion_engine_gui.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(i);
        int j = (width - xSize) / 2;
        int k = (height - ySize) / 2;
        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
        if(tileEngine.isBurning())
        {
            int l = tileEngine.getBurnTimeRemainingScaled(58);
            drawTexturedModalRect(j + 104, (k + 19 + 58) - l, 176, 58 - l, 16, l + 2);
        }        
    }
}
