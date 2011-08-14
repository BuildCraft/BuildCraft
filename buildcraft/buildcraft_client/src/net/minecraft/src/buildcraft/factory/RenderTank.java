package net.minecraft.src.buildcraft.factory;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.RenderEntityBlock.BlockInterface;
import net.minecraft.src.forge.ForgeHooksClient;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.MinecraftForgeClient;

public class RenderTank extends TileEntitySpecialRenderer {

	final static private int displayStages = 100;
	
	private int [] stage = new int [displayStages];	
	
    private void compileDisplayLists() {
    	
		BlockInterface block = new BlockInterface();
		block.texture = 12 * 16 + 13;
		
    	for (int s = 0; s < displayStages; ++s) {
    		stage [s] = GLAllocation.generateDisplayLists(1);
    		GL11.glNewList(stage [s], 4864 /*GL_COMPILE*/);
    		
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
    }

	public RenderTank () {
		//constructor:
		compileDisplayLists();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {		
		
		TileTank tank = ((TileTank) tileentity);
		
		if (tank.getLiquidQuantity() == 0) {
			return;
		}
			
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);
			
		MinecraftForgeClient.bindTexture(BuildCraftCore.customBuildCraftTexture);		

		GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
		
		GL11.glCallList(stage[(int) ((float) tank.getLiquidQuantity() / (float) (tank.getCapacity()) * (float) (displayStages - 1))]);
					
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();
	}	
}
