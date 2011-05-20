package net.minecraft.src.buildcraft.core;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.Entity;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.ModLoader;
import net.minecraft.src.Render;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.Tessellator;
import net.minecraft.src.World;

public class RenderEntityBlock extends Render {

    private RenderBlocks blockRender;

    private class BlockInterface {
        public double minX;
        public double minY;
        public double minZ;
        public double maxX;
        public double maxY;
        public double maxZ;
        
        public Block baseBlock = Block.sand;
        
        public int texture = -1;

        public int getBlockTextureFromSide(int i)
        {
        	if (texture == -1) {
        		return baseBlock.getBlockTextureFromSide (i);
        	} else {
        		return texture;
        	}
        }
        
        public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j, int k)
        {
            return baseBlock.getBlockBrightness(iblockaccess, i, j, k);
        }
    }
    
    
    public RenderEntityBlock () {
    	blockRender = new RenderBlocks();    
    }
	
	@Override
	public void doRender(Entity entity, double i, double j, double k,
			float f, float f1) {		
		doRenderBlock ((EntityBlock) entity, i, j, k);
	}
	
	public void doRenderBlock(EntityBlock entity, double i, double j, double k) {		
		shadowSize = entity.shadowSize;
		World world = entity.worldObj;
		blockRender.blockAccess = ModLoader.getMinecraftInstance().theWorld;
		BlockInterface util = new BlockInterface(); 
		util.texture = entity.texture;				
		
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
			
			lightX = (int) (Math.floor(entity.posX) + iBase);
			lightY = (int) (Math.floor(entity.posY) + jBase);
			lightZ = (int) (Math.floor(entity.posZ) + kBase);
			
			GL11.glDisable(2896 /*GL_LIGHTING*/);
					renderBlockFallingSand(util, world,
							lightX,
							lightY,
							lightZ);
			GL11.glEnable(2896 /*GL_LIGHTING*/);
			GL11.glPopMatrix();

		}		
	}
	
	 public void renderBlockFallingSand(BlockInterface block, World world, int i, int j, int k)
	    {
	        float f = 0.5F;
	        float f1 = 1.0F;
	        float f2 = 0.8F;
	        float f3 = 0.6F;
	        Tessellator tessellator = Tessellator.instance;
	        tessellator.startDrawingQuads();
	        float f4 = block.getBlockBrightness(world, i, j, k);
	        float f5 = block.getBlockBrightness(world, i, j, k);
	        if(f5 < f4)
	        {
	            f5 = f4;
	        }
	        tessellator.setColorOpaque_F(f * f5, f * f5, f * f5);
	        renderBottomFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(0));
	        f5 = block.getBlockBrightness(world, i, j, k);
	        if(f5 < f4)
	        {
	            f5 = f4;
	        }
	        tessellator.setColorOpaque_F(f1 * f5, f1 * f5, f1 * f5);
	        renderTopFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(1));
	        f5 = block.getBlockBrightness(world, i, j, k);
	        if(f5 < f4)
	        {
	            f5 = f4;
	        }
	        tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
	        renderEastFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(2));
	        f5 = block.getBlockBrightness(world, i, j, k);
	        if(f5 < f4)
	        {
	            f5 = f4;
	        }
	        tessellator.setColorOpaque_F(f2 * f5, f2 * f5, f2 * f5);
	        renderWestFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(3));
	        f5 = block.getBlockBrightness(world, i, j, k);
	        if(f5 < f4)
	        {
	            f5 = f4;
	        }
	        tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
	        renderNorthFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(4));
	        f5 = block.getBlockBrightness(world, i, j, k);
	        if(f5 < f4)
	        {
	            f5 = f4;
	        }
	        tessellator.setColorOpaque_F(f3 * f5, f3 * f5, f3 * f5);
	        renderSouthFace(block, -0.5D, -0.5D, -0.5D, block.getBlockTextureFromSide(5));
	        tessellator.draw();
	    }
	 
	 public void renderBottomFace(BlockInterface block, double d, double d1, double d2, 
	            int i)
	    {
	        Tessellator tessellator = Tessellator.instance;
	        if(blockRender.overrideBlockTexture >= 0)
	        {
	            i = blockRender.overrideBlockTexture;
	        }
	        int j = (i & 0xf) << 4;
	        int k = i & 0xf0;
	        double d3 = ((double)j + block.minX * 16D) / 256D;
	        double d4 = (((double)j + block.maxX * 16D) - 0.01D) / 256D;
	        double d5 = ((double)k + block.minZ * 16D) / 256D;
	        double d6 = (((double)k + block.maxZ * 16D) - 0.01D) / 256D;
	        if(block.minX < 0.0D || block.maxX > 1.0D)
	        {
	            d3 = ((float)j + 0.0F) / 256F;
	            d4 = ((float)j + 15.99F) / 256F;
	        }
	        if(block.minZ < 0.0D || block.maxZ > 1.0D)
	        {
	            d5 = ((float)k + 0.0F) / 256F;
	            d6 = ((float)k + 15.99F) / 256F;
	        }
	        double d7 = d + block.minX;
	        double d8 = d + block.maxX;
	        double d9 = d1 + block.minY;
	        double d10 = d2 + block.minZ;
	        double d11 = d2 + block.maxZ;
	        if(blockRender.enableAO)
	        {
	            tessellator.setColorOpaque_F(blockRender.colorRedTopLeft, blockRender.colorGreenTopLeft, blockRender.colorBlueTopLeft);
	            tessellator.addVertexWithUV(d7, d9, d11, d3, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomLeft, blockRender.colorGreenBottomLeft, blockRender.colorBlueBottomLeft);
	            tessellator.addVertexWithUV(d7, d9, d10, d3, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomRight, blockRender.colorGreenBottomRight, blockRender.colorBlueBottomRight);
	            tessellator.addVertexWithUV(d8, d9, d10, d4, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedTopRight, blockRender.colorGreenTopRight, blockRender.colorBlueTopRight);
	            tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
	        } else
	        {
	            tessellator.addVertexWithUV(d7, d9, d11, d3, d6);
	            tessellator.addVertexWithUV(d7, d9, d10, d3, d5);
	            tessellator.addVertexWithUV(d8, d9, d10, d4, d5);
	            tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
	        }
	    }

	    public void renderTopFace(BlockInterface block, double d, double d1, double d2, 
	            int i)
	    {
	        Tessellator tessellator = Tessellator.instance;
	        if(blockRender.overrideBlockTexture >= 0)
	        {
	            i = blockRender.overrideBlockTexture;
	        }
	        int j = (i & 0xf) << 4;
	        int k = i & 0xf0;
	        double d3 = ((double)j + block.minX * 16D) / 256D;
	        double d4 = (((double)j + block.maxX * 16D) - 0.01D) / 256D;
	        double d5 = ((double)k + block.minZ * 16D) / 256D;
	        double d6 = (((double)k + block.maxZ * 16D) - 0.01D) / 256D;
	        if(block.minX < 0.0D || block.maxX > 1.0D)
	        {
	            d3 = ((float)j + 0.0F) / 256F;
	            d4 = ((float)j + 15.99F) / 256F;
	        }
	        if(block.minZ < 0.0D || block.maxZ > 1.0D)
	        {
	            d5 = ((float)k + 0.0F) / 256F;
	            d6 = ((float)k + 15.99F) / 256F;
	        }
	        double d7 = d + block.minX;
	        double d8 = d + block.maxX;
	        double d9 = d1 + block.maxY;
	        double d10 = d2 + block.minZ;
	        double d11 = d2 + block.maxZ;
	        if(blockRender.enableAO)
	        {
	            tessellator.setColorOpaque_F(blockRender.colorRedTopLeft, blockRender.colorGreenTopLeft, blockRender.colorBlueTopLeft);
	            tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomLeft, blockRender.colorGreenBottomLeft, blockRender.colorBlueBottomLeft);
	            tessellator.addVertexWithUV(d8, d9, d10, d4, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomRight, blockRender.colorGreenBottomRight, blockRender.colorBlueBottomRight);
	            tessellator.addVertexWithUV(d7, d9, d10, d3, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedTopRight, blockRender.colorGreenTopRight, blockRender.colorBlueTopRight);
	            tessellator.addVertexWithUV(d7, d9, d11, d3, d6);
	        } else
	        {
	            tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
	            tessellator.addVertexWithUV(d8, d9, d10, d4, d5);
	            tessellator.addVertexWithUV(d7, d9, d10, d3, d5);
	            tessellator.addVertexWithUV(d7, d9, d11, d3, d6);
	        }
	    }

	    
	    public void renderEastFace(BlockInterface block, double d, double d1, double d2, 
	            int i)
	    {
	        Tessellator tessellator = Tessellator.instance;
	        if(blockRender.overrideBlockTexture >= 0)
	        {
	            i = blockRender.overrideBlockTexture;
	        }
	        int j = (i & 0xf) << 4;
	        int k = i & 0xf0;
	        double d3 = ((double)j + block.minX * 16D) / 256D;
	        double d4 = (((double)j + block.maxX * 16D) - 0.01D) / 256D;
	        double d5 = ((double)k + block.minY * 16D) / 256D;
	        double d6 = (((double)k + block.maxY * 16D) - 0.01D) / 256D;
	        if(blockRender.flipTexture)
	        {
	            double d7 = d3;
	            d3 = d4;
	            d4 = d7;
	        }
	        if(block.minX < 0.0D || block.maxX > 1.0D)
	        {
	            d3 = ((float)j + 0.0F) / 256F;
	            d4 = ((float)j + 15.99F) / 256F;
	        }
	        if(block.minY < 0.0D || block.maxY > 1.0D)
	        {
	            d5 = ((float)k + 0.0F) / 256F;
	            d6 = ((float)k + 15.99F) / 256F;
	        }
	        double d8 = d + block.minX;
	        double d9 = d + block.maxX;
	        double d10 = d1 + block.minY;
	        double d11 = d1 + block.maxY;
	        double d12 = d2 + block.minZ;
	        if(blockRender.enableAO)
	        {
	            tessellator.setColorOpaque_F(blockRender.colorRedTopLeft, blockRender.colorGreenTopLeft, blockRender.colorBlueTopLeft);
	            tessellator.addVertexWithUV(d8, d11, d12, d4, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomLeft, blockRender.colorGreenBottomLeft, blockRender.colorBlueBottomLeft);
	            tessellator.addVertexWithUV(d9, d11, d12, d3, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomRight, blockRender.colorGreenBottomRight, blockRender.colorBlueBottomRight);
	            tessellator.addVertexWithUV(d9, d10, d12, d3, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedTopRight, blockRender.colorGreenTopRight, blockRender.colorBlueTopRight);
	            tessellator.addVertexWithUV(d8, d10, d12, d4, d6);
	        } else
	        {
	            tessellator.addVertexWithUV(d8, d11, d12, d4, d5);
	            tessellator.addVertexWithUV(d9, d11, d12, d3, d5);
	            tessellator.addVertexWithUV(d9, d10, d12, d3, d6);
	            tessellator.addVertexWithUV(d8, d10, d12, d4, d6);
	        }
	    }

	    public void renderWestFace(BlockInterface block, double d, double d1, double d2, 
	            int i)
	    {
	        Tessellator tessellator = Tessellator.instance;
	        if(blockRender.overrideBlockTexture >= 0)
	        {
	            i = blockRender.overrideBlockTexture;
	        }
	        int j = (i & 0xf) << 4;
	        int k = i & 0xf0;
	        double d3 = ((double)j + block.minX * 16D) / 256D;
	        double d4 = (((double)j + block.maxX * 16D) - 0.01D) / 256D;
	        double d5 = ((double)k + block.minY * 16D) / 256D;
	        double d6 = (((double)k + block.maxY * 16D) - 0.01D) / 256D;
	        if(blockRender.flipTexture)
	        {
	            double d7 = d3;
	            d3 = d4;
	            d4 = d7;
	        }
	        if(block.minX < 0.0D || block.maxX > 1.0D)
	        {
	            d3 = ((float)j + 0.0F) / 256F;
	            d4 = ((float)j + 15.99F) / 256F;
	        }
	        if(block.minY < 0.0D || block.maxY > 1.0D)
	        {
	            d5 = ((float)k + 0.0F) / 256F;
	            d6 = ((float)k + 15.99F) / 256F;
	        }
	        double d8 = d + block.minX;
	        double d9 = d + block.maxX;
	        double d10 = d1 + block.minY;
	        double d11 = d1 + block.maxY;
	        double d12 = d2 + block.maxZ;
	        if(blockRender.enableAO)
	        {
	            tessellator.setColorOpaque_F(blockRender.colorRedTopLeft, blockRender.colorGreenTopLeft, blockRender.colorBlueTopLeft);
	            tessellator.addVertexWithUV(d8, d11, d12, d3, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomLeft, blockRender.colorGreenBottomLeft, blockRender.colorBlueBottomLeft);
	            tessellator.addVertexWithUV(d8, d10, d12, d3, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomRight, blockRender.colorGreenBottomRight, blockRender.colorBlueBottomRight);
	            tessellator.addVertexWithUV(d9, d10, d12, d4, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedTopRight, blockRender.colorGreenTopRight, blockRender.colorBlueTopRight);
	            tessellator.addVertexWithUV(d9, d11, d12, d4, d5);
	        } else
	        {
	            tessellator.addVertexWithUV(d8, d11, d12, d3, d5);
	            tessellator.addVertexWithUV(d8, d10, d12, d3, d6);
	            tessellator.addVertexWithUV(d9, d10, d12, d4, d6);
	            tessellator.addVertexWithUV(d9, d11, d12, d4, d5);
	        }
	    }

	    public void renderNorthFace(BlockInterface block, double d, double d1, double d2, 
	            int i)
	    {
	        Tessellator tessellator = Tessellator.instance;
	        if(blockRender.overrideBlockTexture >= 0)
	        {
	            i = blockRender.overrideBlockTexture;
	        }
	        int j = (i & 0xf) << 4;
	        int k = i & 0xf0;
	        double d3 = ((double)j + block.minZ * 16D) / 256D;
	        double d4 = (((double)j + block.maxZ * 16D) - 0.01D) / 256D;
	        double d5 = ((double)k + block.minY * 16D) / 256D;
	        double d6 = (((double)k + block.maxY * 16D) - 0.01D) / 256D;
	        if(blockRender.flipTexture)
	        {
	            double d7 = d3;
	            d3 = d4;
	            d4 = d7;
	        }
	        if(block.minZ < 0.0D || block.maxZ > 1.0D)
	        {
	            d3 = ((float)j + 0.0F) / 256F;
	            d4 = ((float)j + 15.99F) / 256F;
	        }
	        if(block.minY < 0.0D || block.maxY > 1.0D)
	        {
	            d5 = ((float)k + 0.0F) / 256F;
	            d6 = ((float)k + 15.99F) / 256F;
	        }
	        double d8 = d + block.minX;
	        double d9 = d1 + block.minY;
	        double d10 = d1 + block.maxY;
	        double d11 = d2 + block.minZ;
	        double d12 = d2 + block.maxZ;
	        if(blockRender.enableAO)
	        {
	            tessellator.setColorOpaque_F(blockRender.colorRedTopLeft, blockRender.colorGreenTopLeft, blockRender.colorBlueTopLeft);
	            tessellator.addVertexWithUV(d8, d10, d12, d4, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomLeft, blockRender.colorGreenBottomLeft, blockRender.colorBlueBottomLeft);
	            tessellator.addVertexWithUV(d8, d10, d11, d3, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomRight, blockRender.colorGreenBottomRight, blockRender.colorBlueBottomRight);
	            tessellator.addVertexWithUV(d8, d9, d11, d3, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedTopRight, blockRender.colorGreenTopRight, blockRender.colorBlueTopRight);
	            tessellator.addVertexWithUV(d8, d9, d12, d4, d6);
	        } else
	        {
	            tessellator.addVertexWithUV(d8, d10, d12, d4, d5);
	            tessellator.addVertexWithUV(d8, d10, d11, d3, d5);
	            tessellator.addVertexWithUV(d8, d9, d11, d3, d6);
	            tessellator.addVertexWithUV(d8, d9, d12, d4, d6);
	        }
	    }

	    public void renderSouthFace(BlockInterface block, double d, double d1, double d2, 
	            int i)
	    {
	        Tessellator tessellator = Tessellator.instance;
	        if(blockRender.overrideBlockTexture >= 0)
	        {
	            i = blockRender.overrideBlockTexture;
	        }
	        int j = (i & 0xf) << 4;
	        int k = i & 0xf0;
	        double d3 = ((double)j + block.minZ * 16D) / 256D;
	        double d4 = (((double)j + block.maxZ * 16D) - 0.01D) / 256D;
	        double d5 = ((double)k + block.minY * 16D) / 256D;
	        double d6 = (((double)k + block.maxY * 16D) - 0.01D) / 256D;
	        if(blockRender.flipTexture)
	        {
	            double d7 = d3;
	            d3 = d4;
	            d4 = d7;
	        }
	        if(block.minZ < 0.0D || block.maxZ > 1.0D)
	        {
	            d3 = ((float)j + 0.0F) / 256F;
	            d4 = ((float)j + 15.99F) / 256F;
	        }
	        if(block.minY < 0.0D || block.maxY > 1.0D)
	        {
	            d5 = ((float)k + 0.0F) / 256F;
	            d6 = ((float)k + 15.99F) / 256F;
	        }
	        double d8 = d + block.maxX;
	        double d9 = d1 + block.minY;
	        double d10 = d1 + block.maxY;
	        double d11 = d2 + block.minZ;
	        double d12 = d2 + block.maxZ;
	        if(blockRender.enableAO)
	        {
	            tessellator.setColorOpaque_F(blockRender.colorRedTopLeft, blockRender.colorGreenTopLeft, blockRender.colorBlueTopLeft);
	            tessellator.addVertexWithUV(d8, d9, d12, d3, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomLeft, blockRender.colorGreenBottomLeft, blockRender.colorBlueBottomLeft);
	            tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
	            tessellator.setColorOpaque_F(blockRender.colorRedBottomRight, blockRender.colorGreenBottomRight, blockRender.colorBlueBottomRight);
	            tessellator.addVertexWithUV(d8, d10, d11, d4, d5);
	            tessellator.setColorOpaque_F(blockRender.colorRedTopRight, blockRender.colorGreenTopRight, blockRender.colorBlueTopRight);
	            tessellator.addVertexWithUV(d8, d10, d12, d3, d5);
	        } else
	        {
	            tessellator.addVertexWithUV(d8, d9, d12, d3, d6);
	            tessellator.addVertexWithUV(d8, d9, d11, d4, d6);
	            tessellator.addVertexWithUV(d8, d10, d11, d4, d5);
	            tessellator.addVertexWithUV(d8, d10, d12, d3, d5);
	        }
	    }

}
