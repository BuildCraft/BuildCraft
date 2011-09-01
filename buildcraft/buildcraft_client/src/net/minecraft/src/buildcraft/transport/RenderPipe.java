package net.minecraft.src.buildcraft.transport;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderManager;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.core.RenderEntityBlock.BlockInterface;
import net.minecraft.src.buildcraft.transport.TilePipe.EntityData;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

public class RenderPipe extends TileEntitySpecialRenderer {

	final static private int displayStages = 40;
	
	private class DisplayList {
		public int [] sideLiquidIn = new int [displayStages];	
		public int [] centerLiquidIn = new int [displayStages * 2];

		public int [] sideLiquidOut = new int [displayStages];	
		public int [] centerLiquidOut = new int [displayStages * 2];
	}
	
	private DisplayList displayLists[] = new DisplayList[Block.blocksList.length];
	
	private final int [] angleY = {0, 0, 270, 90, 0, 180};
	private final int [] angleZ = {90, 270, 0, 0, 0, 0};
	
    private RenderBlocks renderBlocks;
    
    private DisplayList getDisplayLists(int liquidId) {
    	if (displayLists [liquidId] != null) {
    		return displayLists [liquidId];
    	}
    	
    	DisplayList d = new DisplayList();
    	displayLists [liquidId] = d;
    	
		BlockInterface block = new BlockInterface();
		block.texture = Block.blocksList [liquidId].blockIndexInTexture;//12 * 16 + 13;
		float size = Utils.pipeMaxPos - Utils.pipeMinPos;
		
    	for (int s = 0; s < displayStages * 2; ++s) {
    		if (s < displayStages) {
        		d.sideLiquidIn [s] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.sideLiquidIn [s], 4864 /*GL_COMPILE*/);    
        		
    			block.minX = 0;
    		} else {
    			d.sideLiquidOut [s - displayStages] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.sideLiquidOut [s - displayStages], 4864 /*GL_COMPILE*/);    
    			
    			block.minX = (s - (float) displayStages) / ((float) displayStages) * size / 2F;
    		}
    		
    		block.minY = Utils.pipeMinPos + 0.01;
    		block.minZ = Utils.pipeMinPos + 0.01;

    		if (s < displayStages) {
    			block.maxX = s / ((float) displayStages - 1) * size / 2F;
    		} else {
    			block.maxX = size / 2F;
    		}
    				
    		block.maxY = Utils.pipeMaxPos - 0.01;
    		block.maxZ = Utils.pipeMaxPos - 0.01;    		

    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();    		
    	}
        
    	for (int s = 0; s < displayStages * 4; ++s) {
    		if (s < displayStages * 2) {
        		d.centerLiquidIn [s] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.centerLiquidIn [s], 4864 /*GL_COMPILE*/);
        		
    			block.minX = Utils.pipeMinPos + 0.01F;
    		} else {
        		d.centerLiquidOut [s - displayStages * 2] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.centerLiquidOut [s - displayStages * 2], 4864 /*GL_COMPILE*/);
    			
				block.minX = Utils.pipeMinPos + 0.01F
						+ (s - (float) displayStages * 2F - 1) / ((float) displayStages * 2F - 1)
						* size;
    		}
    		
    		block.minY = Utils.pipeMinPos + 0.01F;
    		block.minZ = Utils.pipeMinPos + 0.01F;
    		
    		if (s < displayStages * 2) {
				block.maxX = Utils.pipeMaxPos - ((size - s
				/ ((float) displayStages * 2F - 1) * size)) - 0.01;
    		} else {
    			block.maxX = Utils.pipeMaxPos - 0.01;
    		}

    		block.maxY = Utils.pipeMaxPos - 0.01;
    		block.maxZ = Utils.pipeMaxPos - 0.01;

    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();
    	}
    	
		return d;
    }

	public RenderPipe () {
		renderBlocks = new RenderBlocks();
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {		
		
		TilePipe pipe = ((TilePipe) tileentity);
		
		renderLiquids (pipe, x, y, z);
		renderSolids (pipe, x, y, z);			

	}	
	
	private void renderLiquids(TilePipe pipe, double x, double y, double z) {
		if (pipe.getLiquidId() == 0) {
			return;
		}
		
		Block block = Block.blocksList [pipe.getLiquidId()];
		
		DisplayList d = getDisplayLists(pipe.getLiquidId());
		
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);
				
		if (block instanceof ITextureProvider) {
			MinecraftForgeClient.bindTexture(((ITextureProvider) block)
					.getTextureFile());
		} else {
			MinecraftForgeClient.bindTexture("/terrain.png");
		}

		GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);		
				
		for (int i = 0; i < 6; ++i) {
			renderSide (angleY [i], angleZ [i], pipe.getSideToCenter(i), pipe.getCenterToSide(i), d);
		}
				
		// CENTER

		if (pipe.getCenterIn () > 0 || pipe.getCenterOut () > 0) {
			renderCenter(pipe.lastFromOrientation, pipe.lastToOrientation,
					pipe.getCenterIn(), pipe.getCenterOut(), d);
		}
	
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();		
	}
	
	private void renderSolids(TilePipe pipe, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		
		for (EntityData data : pipe.travelingEntities.values()) {
			doRenderItem(data.item, x + data.item.posX - pipe.xCoord, y
					+ data.item.posY - pipe.yCoord, z + data.item.posZ
					- pipe.zCoord, pipe.worldObj.getLightBrightness(
					pipe.xCoord, pipe.yCoord, pipe.zCoord));
		}
			
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();		
	}
	
	private void renderSide(float angleY, float angleZ, float sideToCenter, float centerToSide, DisplayList d) {
		if (sideToCenter == 0 && centerToSide == 0) {
			return;
		}
				
		GL11.glPushMatrix();		
		
		GL11.glRotatef(angleY, 0, 1, 0);
		GL11.glRotatef(angleZ, 0, 0, 1);
		
		if (sideToCenter + centerToSide >= BuildCraftCore.BUCKET_VOLUME / 4) {
			GL11.glCallList(d.sideLiquidIn[displayStages - 1]);
		} else {
			if (sideToCenter > 0) {
				GL11.glCallList(d.sideLiquidIn[(int) (sideToCenter
						/ (BuildCraftCore.BUCKET_VOLUME / 4F) * (displayStages - 1))]);
			}

			if (centerToSide > 0) {
				GL11.glCallList(d.sideLiquidOut[(displayStages - 1)
						- (int) (centerToSide / (BuildCraftCore.BUCKET_VOLUME / 4F) * (displayStages - 1))]);
			}
		}

		GL11.glPopMatrix();
	}
	
	private void renderCenter(Orientations lastFromOrientation, Orientations lastToOrientation, float centerIn, float centerOut, DisplayList d) {
		GL11.glPushMatrix();					

		if (centerIn + centerOut >= BuildCraftCore.BUCKET_VOLUME / 2) {
			GL11.glRotatef(angleY [lastFromOrientation.ordinal()], 0, 1, 0);
			GL11.glRotatef(angleZ [lastFromOrientation.ordinal()], 0, 0, 1);
			GL11.glCallList(d.centerLiquidIn [displayStages * 2 - 1]);
		} else {
			if (centerIn > 0) {
				GL11.glRotatef(angleY [lastFromOrientation.ordinal()], 0, 1, 0);
				GL11.glRotatef(angleZ [lastFromOrientation.ordinal()], 0, 0, 1);
			} else {
				GL11.glRotatef(angleY [lastToOrientation.reverse().ordinal()], 0, 1, 0);
				GL11.glRotatef(angleZ [lastToOrientation.reverse().ordinal()], 0, 0, 1);
			}			
			
			if (centerIn > 0) {
				GL11.glCallList(d.centerLiquidIn[(int) (centerIn / (BuildCraftCore.BUCKET_VOLUME / 2F) * (displayStages * 2 - 1))]);
			}

			if (centerOut > 0) {
				GL11.glCallList(d.centerLiquidOut[(displayStages * 2 - 1)
						- (int) (centerOut
								/ (BuildCraftCore.BUCKET_VOLUME / 2F) * (displayStages * 2 - 1))]);
			}
		}

		GL11.glPopMatrix();
	}

    private void doRenderItem(EntityPassiveItem entityitem, double x, double y, double z, double brigntess)
    {        
    	if (entityitem == null || entityitem.item == null) {
    		return;
    	}
    	
        ItemStack itemstack = entityitem.item;
        
        GL11.glPushMatrix();

        byte byte0 = 1;
        if(entityitem.item.stackSize > 1)
        {
            byte0 = 2;
        }
        if(entityitem.item.stackSize > 5)
        {
            byte0 = 3;
        }
        if(entityitem.item.stackSize > 20)
        {
            byte0 = 4;
        }
        
		GL11.glTranslatef((float) x, (float) y, (float) z);		
		
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);   
                
		if (itemstack.itemID < Block.blocksList.length
				&& itemstack.itemID > 0				
				&& RenderBlocks
						.renderItemIn3d(Block.blocksList[itemstack.itemID]
								.getRenderType()))
        {
			GL11.glTranslatef(0, 0.25F, 0);
			Block block = Block.blocksList [itemstack.itemID];
			
			if (block instanceof ITextureProvider) {
				MinecraftForgeClient.bindTexture(((ITextureProvider) block)
						.getTextureFile());
			} else {
				MinecraftForgeClient.bindTexture("/terrain.png");
			}
	        
            float f4 = 0.25F;
            if(!Block.blocksList[itemstack.itemID].renderAsNormalBlock() && itemstack.itemID != Block.stairSingle.blockID)
            {
                f4 = 0.5F;
            }
            GL11.glScalef(f4, f4, f4);
            for(int j = 0; j < byte0; j++)
            {
                GL11.glPushMatrix();
				renderBlocks.renderBlockOnInventory(
						Block.blocksList[itemstack.itemID],
						itemstack.getItemDamage(), (float) brigntess);
                GL11.glPopMatrix();
            }

        } else
        {
        	GL11.glTranslatef(0, 0.10F, 0);	
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            int i = itemstack.getIconIndex();
            if (itemstack.getItem() instanceof ITextureProvider) {
				MinecraftForgeClient.bindTexture(((ITextureProvider) itemstack
						.getItem()).getTextureFile());
            } else if(itemstack.itemID < 256) {
				MinecraftForgeClient.bindTexture("/terrain.png");
            } else {
				MinecraftForgeClient.bindTexture("/gui/items.png");
            }
            Tessellator tessellator = Tessellator.instance;
            float f6 = (float)((i % 16) * 16 + 0) / 256F;
            float f8 = (float)((i % 16) * 16 + 16) / 256F;
            float f10 = (float)((i / 16) * 16 + 0) / 256F;
            float f11 = (float)((i / 16) * 16 + 16) / 256F;
            float f12 = 1.0F;
            float f13 = 0.5F;
            float f14 = 0.25F;
            for(int k = 0; k < byte0; k++)
            {
                GL11.glPushMatrix();
              
                GL11.glRotatef(180F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
                tessellator.addVertexWithUV(0.0F - f13, 0.0F - f14, 0.0D, f6, f11);
                tessellator.addVertexWithUV(f12 - f13, 0.0F - f14, 0.0D, f8, f11);
                tessellator.addVertexWithUV(f12 - f13, 1.0F - f14, 0.0D, f8, f10);
                tessellator.addVertexWithUV(0.0F - f13, 1.0F - f14, 0.0D, f6, f10);
                tessellator.draw();
                GL11.glPopMatrix();
            }

        }
        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glPopMatrix();
    }
}
