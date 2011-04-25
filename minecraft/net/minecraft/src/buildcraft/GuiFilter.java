package net.minecraft.src.buildcraft;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.CraftingInventoryCB;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;

public class GuiFilter extends GuiContainer {
	
	IInventory playerInventory;
	IInventory filterInventory;
	
	static class CraftingInv extends CraftingInventoryCB {
		
		public CraftingInv (IInventory playerInventory, IInventory filterInventory) {
			for(int k = 0; k < 6; k++)
	        {
	            for(int j1 = 0; j1 < 9; j1++)
	            {
	                addSlot(new Slot(filterInventory, j1 + k * 9, 8 + j1 * 18, 18 + k * 18));
	            }

	        }

	        for(int l = 0; l < 3; l++)
	        {
	            for(int k1 = 0; k1 < 9; k1++)
	            {
	                addSlot(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 140 + l * 18));
	            }

	        }

	        for(int i1 = 0; i1 < 9; i1++)
	        {
	            addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 198));
	        }
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer entityplayer) {
			return true;
		}
		
	}
	
	public GuiFilter(IInventory playerInventory, IInventory filterInventory) {
		super(new CraftingInv(playerInventory, filterInventory));
		this.playerInventory = playerInventory;
		this.filterInventory = filterInventory;
		xSize = 175;
		ySize = 225;
	}
	
    protected void drawGuiContainerForegroundLayer()
    {
        fontRenderer.drawString(filterInventory.getInvName(), 8, 6, 0x404040);
        fontRenderer.drawString(playerInventory.getInvName(), 8, ySize - 97, 0x404040);        
    }
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
	        int i = mc.renderEngine.getTexture("/buildcraft_gui/filter.png");
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        mc.renderEngine.bindTexture(i);
	        int j = (width - xSize) / 2;
	        int k = (height - ySize) / 2;
	        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	    }

	int inventoryRows = 6;
}
