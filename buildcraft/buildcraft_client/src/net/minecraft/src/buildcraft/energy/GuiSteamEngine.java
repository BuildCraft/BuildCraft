package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;

import org.lwjgl.opengl.GL11;

public class GuiSteamEngine extends GuiContainer {

    private TileEngine tileEngine;

	public GuiSteamEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine)
    {
        super(new ContainerEngine(inventoryplayer, tileEngine));
        this.tileEngine = tileEngine;
    }

    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString("Steam Engine", 60, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
    }

    protected void drawGuiContainerBackgroundLayer(float f)
    {
		int i = mc.renderEngine
				.getTexture("/net/minecraft/src/buildcraft/energy/gui/steam_engine_gui.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(i);
        int j = (width - xSize) / 2;
        int k = (height - ySize) / 2;
        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
        if(tileEngine.isBurning())
        {
            int l = tileEngine.getBurnTimeRemainingScaled(12);
            drawTexturedModalRect(j + 80, (k + 24 + 12) - l, 176, 12 - l, 14, l + 2);
        }        
    }
}
