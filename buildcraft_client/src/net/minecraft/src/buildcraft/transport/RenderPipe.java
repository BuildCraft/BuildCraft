/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityItem;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderEngine;
import net.minecraft.src.RenderManager;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock.BlockInterface;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.PipeTransportItems.EntityData;
import net.minecraft.src.buildcraft.transport.PipeTransportLiquids.LiquidBuffer;
import net.minecraft.src.forge.ForgeHooksClient;
import net.minecraft.src.forge.IItemRenderer;
import net.minecraft.src.forge.IItemRenderer.ItemRenderType;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

public class RenderPipe extends TileEntitySpecialRenderer {
	
	final static private int maxPower = 1000;
	
	final static private int displayLiquidStages = 40;
	
	private final static EntityItem dummyEntityItem = new EntityItem(null); 
	
	private class DisplayLiquidList {
		public int [] sideHorizontal = new int [displayLiquidStages];
		public int [] sideVertical = new int [displayLiquidStages];
		public int [] centerHorizontal = new int [displayLiquidStages];
		public int [] centerVertical = new int [displayLiquidStages];		
	}
	
	private HashMap<Integer, DisplayLiquidList> displayLiquidLists = new HashMap<Integer, DisplayLiquidList>();
	
	private final int [] angleY = {0, 0, 270, 90, 0, 180};
	private final int [] angleZ = {90, 270, 0, 0, 0, 0};
	
	final static private int displayPowerStages = 80;
	
	public int [] displayPowerList = new int [displayPowerStages];	
	public double [] displayPowerLimits = new double [displayPowerStages];
	
	private RenderBlocks renderBlocks;

	public RenderPipe() {
		renderBlocks = new RenderBlocks();
		
		initializeDisplayPowerList ();
	}
	
    private DisplayLiquidList getDisplayLiquidLists(int liquidId) {
    	if (displayLiquidLists.containsKey (liquidId)) {
    		return displayLiquidLists.get(liquidId);
    	}
    	
    	DisplayLiquidList d = new DisplayLiquidList();
    	displayLiquidLists.put(liquidId, d);
    	
		BlockInterface block = new BlockInterface();
		if (liquidId < 256) {
			block.texture = Block.blocksList [liquidId].blockIndexInTexture;
		} else {
			block.texture = Item.itemsList [liquidId].getIconFromDamage(0);
		}
		float size = Utils.pipeMaxPos - Utils.pipeMinPos;
		
		// render size
		
    	for (int s = 0; s < displayLiquidStages; ++s) {
    		float ratio = (float) s / (float) displayLiquidStages ;
    		
    		// SIDE HORIZONTAL
    		
    		d.sideHorizontal [s] = GLAllocation.generateDisplayLists(1);
    		GL11.glNewList(d.sideHorizontal [s], 4864 /*GL_COMPILE*/);    
        	
    		block.minX = 0.0F;    		
    		block.minZ = Utils.pipeMinPos + 0.01F;
    		
    		block.maxX = block.minX + size / 2F + 0.01F;
    		block.maxZ = block.minZ + size - 0.02F;

    		block.minY = Utils.pipeMinPos + 0.01F;
			block.maxY = block.minY + (size - 0.02F) * ratio;
    		
    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();
    		
    		// SIDE VERTICAL
    		
    		d.sideVertical [s] = GLAllocation.generateDisplayLists(1);
    		GL11.glNewList(d.sideVertical [s], 4864 /*GL_COMPILE*/);    
        	
    		block.minY = Utils.pipeMaxPos - 0.01;
    		block.maxY = 1;
    		
    		block.minX = 0.5 - (size / 2 - 0.01) * ratio;
    		block.maxX = 0.5 + (size / 2 - 0.01) * ratio;
    		
    		block.minZ = 0.5 - (size / 2 - 0.01) * ratio;
    		block.maxZ = 0.5 + (size / 2 - 0.01) * ratio;
    		
    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();
    		
    		// CENTER HORIZONTAL

    		d.centerHorizontal [s] = GLAllocation.generateDisplayLists(1);
    		GL11.glNewList(d.centerHorizontal [s], 4864 /*GL_COMPILE*/);    
        	
    		block.minX = Utils.pipeMinPos + 0.01;    		
    		block.minZ = Utils.pipeMinPos + 0.01;
    		
    		block.maxX = block.minX + size - 0.02;
    		block.maxZ = block.minZ + size - 0.02;

    		block.minY = Utils.pipeMinPos + 0.01;
			block.maxY = block.minY + (size - 0.02F) * ratio;
    		
    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();
    		
    		// CENTER VERTICAL

    		d.centerVertical [s] = GLAllocation.generateDisplayLists(1);
    		GL11.glNewList(d.centerVertical [s], 4864 /*GL_COMPILE*/);    
        	
    		block.minY = Utils.pipeMinPos + 0.01;
    		block.maxY = Utils.pipeMaxPos - 0.01;
    		
    		block.minX = 0.5 - (size / 2 - 0.02) * ratio;
    		block.maxX = 0.5 + (size / 2 - 0.02) * ratio;
    		
    		block.minZ = 0.5 - (size / 2 - 0.02) * ratio;
    		block.maxZ = 0.5 + (size / 2 - 0.02) * ratio;
    		
    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();
    		
    	}
    	
		return d;
    }
    
    private void initializeDisplayPowerList () {    	
		BlockInterface block = new BlockInterface();
		block.texture = 0 * 16 + 4;
		
		float size = Utils.pipeMaxPos - Utils.pipeMinPos;
		
    	for (int s = 0; s < displayPowerStages; ++s) {
    		displayPowerList [s] = GLAllocation.generateDisplayLists(1); 
    		GL11.glNewList(displayPowerList [s], 4864 /*GL_COMPILE*/);
    		    			
    		float minSize = 0.005F;
    		
    		float unit = (size - minSize) / 2F / (float) displayPowerStages;
    		
    		block.minY = 0.5 - (minSize / 2F) - unit * (float) s;
    		block.maxY = 0.5 + (minSize / 2F) + unit * (float) s;
    		
    		block.minZ = 0.5 - (minSize / 2F) - unit * (float) s;
    		block.maxZ = 0.5 + (minSize / 2F) + unit * (float) s;
    		
    		block.minX = 0;    
    		block.maxX = 0.5 + (minSize / 2F) + unit * (float) s;
    		    		
    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();    		
    	}
    	
    	for (int i = 0; i < displayPowerStages; ++i) {
			displayPowerLimits[displayPowerStages - i - 1] = maxPower
					- Math.sqrt(maxPower * maxPower / (displayPowerStages - 1)
							* i);		
    	}
    }

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {

		TileGenericPipe pipe = ((TileGenericPipe) tileentity);
		
		if (pipe.pipe == null) {
			return;
		}
		
		if (pipe.pipe.transport instanceof PipeTransportLiquids) {
			renderLiquids(pipe.pipe, x, y, z);
		}

		if (pipe.pipe.transport instanceof PipeTransportItems) {
			renderSolids(pipe.pipe, x, y, z);
		}
		
		if (pipe.pipe.transport instanceof PipeTransportPower) {
			renderPower(pipe.pipe, x, y, z);
		}
	}
	
	private void renderPower(Pipe pipe, double x, double y, double z) {
		PipeTransportPower pow = (PipeTransportPower) pipe.transport;
		
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		
		MinecraftForgeClient.bindTexture(BuildCraftCore.customBuildCraftTexture);
		
		GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
		
		for (int i = 0; i < 6; ++i) {
			GL11.glPushMatrix();		
			
			GL11.glRotatef(angleY [i], 0, 1, 0);
			GL11.glRotatef(angleZ [i], 0, 0, 1);
			
			if (pow.displayPower [i] >= 1.0) {
				int stage = 0;
				
				for ( ; stage < displayPowerStages; ++stage) {
					if (displayPowerLimits [stage] > pow.displayPower [i]) {
						break;
					}
				}
				
				if (stage < displayPowerList.length) {
					GL11.glCallList(displayPowerList [stage]);
				} else {
					GL11.glCallList(displayPowerList [displayPowerList.length - 1]);
				}
			} 
			
			GL11.glPopMatrix();		
		}
				
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();				
	}

	private void renderLiquids(Pipe pipe, double x, double y, double z) {		
		PipeTransportLiquids liq = (PipeTransportLiquids) pipe.transport;
		
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);

		GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);		
				
		// sides
		
		boolean sides = false, above = false;
		
		for (int i = 0; i < 6; ++i) {
			if (liq.getSide(i) > 0) {				
				DisplayLiquidList d = getListFromBuffer(liq.side [i]);
				
				if (d == null) {
					continue;
				}
				
				int stage = (int) ((float) liq.getSide(i)
						/ (float) (PipeTransportLiquids.LIQUID_IN_PIPE) * (float) (displayLiquidStages - 1));

				GL11.glPushMatrix();
				int list = 0;

				switch (Orientations.values() [i]) {
				case YPos:
					above = true;
					list = d.sideVertical [stage];
					break;
				case YNeg:
					GL11.glTranslatef(0, -0.75F, 0);
					list = d.sideVertical [stage];					
					break;
				case XPos: case XNeg: case ZPos: case ZNeg:
					sides = true;
					GL11.glRotatef(angleY [i], 0, 1, 0);
					GL11.glRotatef(angleZ [i], 0, 0, 1);
					list = d.sideHorizontal [stage];
					break;
				}				

				GL11.glCallList(list);
				GL11.glPopMatrix();	
			}						
		}
				
		// CENTER

		if (liq.getCenter () > 0) {
			DisplayLiquidList d = getListFromBuffer(liq.center);
			
			if (d != null) {
				int stage = (int) ((float) liq.getCenter()
						/ (float) (PipeTransportLiquids.LIQUID_IN_PIPE) * (float) (displayLiquidStages - 1));

				if (above) {
					GL11.glCallList(d.centerVertical[stage]);
				} 

				if (!above || sides) {
					GL11.glCallList(d.centerHorizontal[stage]);	
				}
			}
			
									
		}
	
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();		
	}

	public DisplayLiquidList getListFromBuffer (LiquidBuffer buf) {
		
		int liquidId = buf.liquidId;
		
		if (liquidId == 0) {
			return null;
		}
		
		Object o = null;
		
		if (liquidId < 256) {
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
		
		return getDisplayLiquidLists(liquidId);
	}
	
	private void renderSolids(Pipe pipe, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		for (EntityData data : ((PipeTransportItems) pipe.transport).travelingEntities
				.values()) {
			doRenderItem(data.item, x + data.item.posX - pipe.xCoord, y
					+ data.item.posY - pipe.yCoord, z + data.item.posZ
					- pipe.zCoord, pipe.worldObj.getLightBrightness(
					pipe.xCoord, pipe.yCoord, pipe.zCoord));
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}
	
	private Random random = new Random ();
	
    public void doRenderItem(EntityPassiveItem entityitem, double d, double d1, double d2, 
            float f1) {
		if (entityitem == null || entityitem.item == null) {
			return;
		}

		ItemStack itemstack = entityitem.item;
		random.setSeed(187L);
		
        GL11.glPushMatrix();
//        float f2 = MathHelper.sin(((float)entityitem.age + f1) / 10F + entityitem.field_804_d) * 0.1F + 0.1F;
//        float f3 = (((float)entityitem.age + f1) / 20F + entityitem.field_804_d) * 57.29578F;
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
//        GL11.glTranslatef((float)d, (float)d1 + f2, (float)d2);
        GL11.glTranslatef((float)d, (float)d1, (float)d2);
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);

        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.ENTITY);
        
        if (customRenderer != null) {
//        	GL11.glRotatef(f3, 0.0F, 1.0F, 0.0F);
        	GL11.glTranslatef(0, 0.25F, 0); // BC SPECIFIC
            loadTexture("/terrain.png");
            ForgeHooksClient.overrideTexture(itemstack.getItem());
            float f4 = 0.25F;
            f4 = 0.5F;
            GL11.glScalef(f4, f4, f4);
            for(int j = 0; j < byte0; j++)
            {
                GL11.glPushMatrix();
                if(j > 0)
                {
                    float f5 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
                    float f7 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
                    float f9 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
                    GL11.glTranslatef(f5, f7, f9);
                }
                RenderPipe.dummyEntityItem.item = itemstack;
               
                customRenderer.renderItem(ItemRenderType.ENTITY, itemstack, renderBlocks, RenderPipe.dummyEntityItem);
                GL11.glPopMatrix();
            }
        } else if(itemstack.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[itemstack.itemID].getRenderType()))
        {
//            GL11.glRotatef(f3, 0.0F, 1.0F, 0.0F);
        	GL11.glTranslatef(0, 0.25F, 0); // BC SPECIFIC
            loadTexture("/terrain.png");
            ForgeHooksClient.overrideTexture(Block.blocksList[itemstack.itemID]);
            float f4 = 0.25F;
            int j = Block.blocksList[itemstack.itemID].getRenderType();
            if(j == 1 || j == 19 || j == 12 || j == 2)
            {
                f4 = 0.5F;
            }
            GL11.glScalef(f4, f4, f4);
            for(int k = 0; k < byte0; k++)
            {
                GL11.glPushMatrix();
                if(k > 0)
                {
                    float f6 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
                    float f9 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
                    float f11 = ((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / f4;
                    GL11.glTranslatef(f6, f9, f11);
                }
                float f7 = 1.0F;
                renderBlocks.renderBlockAsItem(Block.blocksList[itemstack.itemID], itemstack.getItemDamage(), f7);
                GL11.glPopMatrix();
            }

        } else
        {
        	GL11.glTranslatef(0, 0.10F, 0); // BC SPECIFIC
        	
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            int i = itemstack.getIconIndex();
            if(itemstack.itemID < Block.blocksList.length
            		&& Block.blocksList[itemstack.itemID] != null) {
                loadTexture("/terrain.png");
                ForgeHooksClient.overrideTexture(Block.blocksList[itemstack.itemID]);
            } else {
                loadTexture("/gui/items.png");
                ForgeHooksClient.overrideTexture(Item.itemsList[itemstack.itemID]);
            }
            Tessellator tessellator = Tessellator.instance;
            float f5 = (float)((i % 16) * 16 + 0) / 256F;
            float f8 = (float)((i % 16) * 16 + 16) / 256F;
            float f10 = (float)((i / 16) * 16 + 0) / 256F;
            float f12 = (float)((i / 16) * 16 + 16) / 256F;
            float f13 = 1.0F;
            float f14 = 0.5F;
            float f15 = 0.25F;
//            if(field_27004_a)
//            {
//                int l = Item.itemsList[itemstack.itemID].getColorFromDamage(itemstack.getItemDamage());
//                float f16 = (float)(l >> 16 & 0xff) / 255F;
//                float f18 = (float)(l >> 8 & 0xff) / 255F;
//                float f20 = (float)(l & 0xff) / 255F;
//                float f22 = 1.0F;
//                GL11.glColor4f(f16 * f22, f18 * f22, f20 * f22, 1.0F);
//            }
            for(int i1 = 0; i1 < byte0; i1++)
            {
                GL11.glPushMatrix();
                if(i1 > 0)
                {
                    float f17 = (random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    float f19 = (random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    float f21 = (random.nextFloat() * 2.0F - 1.0F) * 0.3F;
                    GL11.glTranslatef(f17, f19, f21);
                }
                GL11.glRotatef(180F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
                tessellator.startDrawingQuads();
                tessellator.setNormal(0.0F, 1.0F, 0.0F);
                tessellator.addVertexWithUV(0.0F - f14, 0.0F - f15, 0.0D, f5, f12);
                tessellator.addVertexWithUV(f13 - f14, 0.0F - f15, 0.0D, f8, f12);
                tessellator.addVertexWithUV(f13 - f14, 1.0F - f15, 0.0D, f8, f10);
                tessellator.addVertexWithUV(0.0F - f14, 1.0F - f15, 0.0D, f5, f10);
                tessellator.draw();
                GL11.glPopMatrix();
            }

        }
        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glPopMatrix();
	}
    
    protected void loadTexture(String s)
    {
        RenderEngine renderengine = RenderManager.instance.renderEngine;
        renderengine.bindTexture(renderengine.getTexture(s));
    }
}
