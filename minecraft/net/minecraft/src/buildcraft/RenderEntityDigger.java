package net.minecraft.src.buildcraft;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.MathHelper;
import net.minecraft.src.Render;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.World;

public class RenderEntityDigger extends Render {

    private RenderBlocks blockRender;
    
    public RenderEntityDigger () {
    	blockRender = new RenderBlocks();
    }
	
	@Override
	public void doRender(Entity entity, double d, double d1, double d2,
			float f, float f1) {
		
//        GL11.glPushMatrix();
//        GL11.glTranslatef((float)d, (float)d1, (float)d2);
//        loadTexture("/terrain.png");
//        Block block = Block.blocksList[entityfallingsand.blockID];
//        World world = entityfallingsand.func_465_i();
//        GL11.glDisable(2896 /*GL_LIGHTING*/);
//        blockRender.renderBlockFallingSand(block, world, MathHelper.floor_double(entityfallingsand.posX), MathHelper.floor_double(entityfallingsand.posY), MathHelper.floor_double(entityfallingsand.posZ));
//        GL11.glEnable(2896 /*GL_LIGHTING*/);
//        GL11.glPopMatrix();

	}

}
