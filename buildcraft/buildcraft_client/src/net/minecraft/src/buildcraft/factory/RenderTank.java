/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.factory;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.Item;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock.BlockInterface;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

public class RenderTank extends TileEntitySpecialRenderer {

	final static private int displayStages = 100;
	
	private HashMap<Integer, int []> stage = new HashMap<Integer, int []> ();	
	
    private int [] getDisplayLists(int liquidId) {
    	
    	if (stage.containsKey(liquidId)) {
    		return stage.get(liquidId);
    	}
    	
    	int [] d = new int [displayStages];
    	stage.put(liquidId, d);
		
		BlockInterface block = new BlockInterface();
		if (liquidId < Block.blocksList.length) {
			block.texture = Block.blocksList [liquidId].blockIndexInTexture;
		} else {
			block.texture = Item.itemsList [liquidId].getIconFromDamage(0);
		}
		
    	for (int s = 0; s < displayStages; ++s) {
    		d [s] = GLAllocation.generateDisplayLists(1);
    		GL11.glNewList(d [s], 4864 /*GL_COMPILE*/);
    		
    		block.minX = 0.125 + 0.01;
    		block.minY = 0;
    		block.minZ = 0.125 + 0.01;
    		
    		block.maxX = 0.875 - 0.01;
    		block.maxY = (float) s / (float) displayStages;
    		block.maxZ = 0.875 - 0.01;
    		
    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);
    		
    		GL11.glEndList();
    	}
    	
    	return d;
    }
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {		
		
		TileTank tank = ((TileTank) tileentity);
		
		int liquidId = tank.getLiquidId();
		
		if (tank.getLiquidQuantity() == 0 || liquidId == 0) {
			return;
		}
		
		int [] d = getDisplayLists(tank.getLiquidId());
			
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		
		Object o = null;
		
		if (liquidId < Block.blocksList.length) {
			o = Block.blocksList [liquidId];
		} else {
			o = Item.itemsList [liquidId];
		}
		
		if (o instanceof ITextureProvider) {
			MinecraftForgeClient.bindTexture(((ITextureProvider) o)
					.getTextureFile());
		} else {
			MinecraftForgeClient.bindTexture("/terrain.png");
		}	

		GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
		
		GL11.glCallList(d[(int) ((float) tank.getLiquidQuantity() / (float) (tank.getCapacity()) * (float) (displayStages - 1))]);
					
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();
	}	
}
