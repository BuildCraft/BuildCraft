package net.minecraft.src.buildcraft;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.CraftingInventoryCB;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;

public class GuiRooter extends GuiContainer {
	
	static class CraftingInv extends CraftingInventoryCB {
		
		public CraftingInv (IInventory playerInventory, IInventory rooterInventory) {
			for(int k = 0; k < 6; k++)
	        {
	            for(int j1 = 0; j1 < 8; j1++)
	            {
	                addSlot(new Slot(rooterInventory, j1 + k * 8, 8 + 18 + j1 * 18, 18 + k * 18));
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
	
	public GuiRooter(IInventory playerInventory, IInventory rooterInventory) {
		super(new CraftingInv(playerInventory, rooterInventory));
		
		xSize = 175;
		ySize = 225;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f) {
	        int i = mc.renderEngine.getTexture("/buildcraft_gui/rooter.png");
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        mc.renderEngine.bindTexture(i);
	        int j = (width - xSize) / 2;
	        int k = (height - ySize) / 2;
	        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	    }

	int inventoryRows = 6;
}
