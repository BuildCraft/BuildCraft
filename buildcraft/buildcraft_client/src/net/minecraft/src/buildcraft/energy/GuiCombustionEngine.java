package net.minecraft.src.buildcraft.energy;

import net.minecraft.src.Block;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

public class GuiCombustionEngine extends GuiContainer {

    private TileEngine tileEngine;

	public GuiCombustionEngine(InventoryPlayer inventoryplayer, TileEngine tileEngine)
    {
        super(new ContainerEngine(inventoryplayer, tileEngine));
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
        
        if(tileEngine.getScaledBurnTime(58) > 0)
        {
            int l = tileEngine.getScaledBurnTime(58);
            
            EngineIron engineIron = ((EngineIron) tileEngine.engine);
            int liquidId = engineIron.liquidId;
            
            Object o = null;
            int liquidImgIndex = 0;
    		
    		if (liquidId < Block.blocksList.length) {
    			o = Block.blocksList [liquidId];
    			liquidImgIndex = Block.blocksList [liquidId].blockIndexInTexture;
    		} else {
    			o = Item.itemsList [liquidId];
    			liquidImgIndex = Item.itemsList [liquidId].getIconFromDamage(0);
    		}

    		if (o instanceof ITextureProvider) {
    			MinecraftForgeClient.bindTexture(((ITextureProvider) o)
    					.getTextureFile());
    		} else {
    			MinecraftForgeClient.bindTexture("/terrain.png");
    		}
    		
    		int imgLine = liquidImgIndex / 16;
    		int imgColumn = liquidImgIndex - imgLine * 16;
            
    		int start = 0;
    		    		
    		while (true) {
    			int x = 0;	
    			
    			if (l > 16) {
    				x = 16;
    				l -= 16;
    			} else {
    				x = l;
    				l = 0;
    			}
    			
    			drawTexturedModalRect(j + 104, k + 19 + 58 - x - start, imgColumn * 16, imgLine * 16, 16, 16 - (16 - x));
    			start = start + 16;
    			
    			if (x == 0 || l == 0) {
    				break;
    			}
    		}
        }    
        		
		mc.renderEngine.bindTexture(i);
		drawTexturedModalRect(j + 104, k + 19, 176, 0, 16, 60);
    }
}
