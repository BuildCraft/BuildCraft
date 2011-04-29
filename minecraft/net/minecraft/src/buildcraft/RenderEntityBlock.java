package net.minecraft.src.buildcraft;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPainting;
import net.minecraft.src.EnumArt;
import net.minecraft.src.ItemRenderer;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Render;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderManager;
import net.minecraft.src.Tessellator;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class RenderEntityBlock extends Render {

    private RenderBlocks blockRender;

    
    public RenderEntityBlock () {
    	blockRender = new RenderBlocks();
    	shadowSize = 0.5F;
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
		
		// TODO: draw the block n time in each direction respecting isize, jsize and ksize (by jumps of 1);
		
		for (int iBase = 0; iBase <= entity.iSize; ++iBase)
			for (int jBase = 0; jBase <= entity.jSize; ++jBase)
				for (int kBase = 0; kBase <= entity.kSize; ++kBase) 
		{					
			util.minX = iBase;
			util.minY = jBase;
			util.minZ = kBase;
			
			double remainX = entity.iSize - iBase;
			double remainY = entity.jSize - jBase;
			double remainZ = entity.kSize - kBase;
			
			util.maxX = iBase + (remainX > 1.0 ? 1.0 : remainX);
			util.maxY = jBase + (remainY > 1.0 ? 1.0 : remainY);
			util.maxZ = kBase + (remainZ > 1.0 ? 1.0 : remainZ);
			
			
//		util.minX = 0;
//		util.minY = 0;
//		util.minZ = 0;
//		
//		util.maxX = 1;
//		util.maxY = 1;
//		util.maxZ = 1;
		
			GL11.glPushMatrix();
			GL11.glTranslatef((float)i, (float)j, (float)k);
			loadTexture("/terrain.png");		
			util.baseBlock = Block.blocksList [entity.blockID];
			GL11.glDisable(2896 /*GL_LIGHTING*/);
					blockRender.renderBlockFallingSand(util, world,
							MathHelper.floor_double(entity.posX),
							MathHelper.floor_double(entity.posY),
							MathHelper.floor_double(entity.posZ));
			GL11.glEnable(2896 /*GL_LIGHTING*/);
			GL11.glPopMatrix();

		}		
	}
	
}
