package net.minecraft.src.buildcraft;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Render;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class RenderEntityBlock extends Render {

    private RenderBlocks blockRender;

    
    
    public RenderEntityBlock () {
    	blockRender = new RenderBlocks();
    	shadowSize = 1.0F;
    }
	
	@Override
	public void doRender(Entity entity, double i, double j, double k,
			float f, float f1) {		
		doRenderBlock ((EntityBlock) entity, i, j, k);
	}
	
	public void doRenderBlock(EntityBlock entity, double i, double j, double k) {		
		World world = entity.worldObj;
		blockRender.blockAccess = ModLoader.getMinecraftInstance().theWorld;
		BlockUtil util = mod_BuildCraft.getInstance().blockUtil; 
		
		for (int iBase = 0; iBase <= entity.iSize; ++iBase)
			for (int jBase = 0; jBase <= entity.jSize; ++jBase)
				for (int kBase = 0; kBase <= entity.kSize; ++kBase) 
		{					
			util.minX = 0;
			util.minY = 0;
			util.minZ = 0;
					
			double remainX = entity.iSize - iBase;
			double remainY = entity.jSize - jBase;
			double remainZ = entity.kSize - kBase;
			
			util.maxX = (remainX > 1.0 ? 1.0 : remainX);
			util.maxY = (remainY > 1.0 ? 1.0 : remainY);
			util.maxZ = (remainZ > 1.0 ? 1.0 : remainZ);			
						
			GL11.glPushMatrix();
			GL11.glTranslatef((float)i + iBase + 0.5F, (float)j + jBase + 0.5F, (float)k + kBase + 0.5F);
			loadTexture("/terrain.png");		
			util.baseBlock = Block.blocksList [entity.blockID];			
			
			int lightX, lightY, lightZ;
			
			lightX = (int) (entity.posX + iBase - 1);
			lightY = (int) (entity.posY + jBase);
			lightZ = (int) (entity.posZ + kBase - 1);
			
			GL11.glDisable(2896 /*GL_LIGHTING*/);
					blockRender.renderBlockFallingSand(util, world,
							lightX,
							lightY,
							lightZ);
			GL11.glEnable(2896 /*GL_LIGHTING*/);
			GL11.glPopMatrix();

		}		
	}
	
}
