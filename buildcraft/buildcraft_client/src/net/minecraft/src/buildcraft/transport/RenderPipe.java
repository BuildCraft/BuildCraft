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
import net.minecraft.src.buildcraft.core.RenderEntityBlock.BlockInterface;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.PipeTransportItems.EntityData;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

public class RenderPipe extends TileEntitySpecialRenderer {
	
	final static private int maxPower = 1000;
	
	final static private int displayLiquidStages = 40;
	
	private class DisplayLiquidList {
		public int [] sideLiquidIn = new int [displayLiquidStages];	
		public int [] centerLiquidIn = new int [displayLiquidStages * 2];

		public int [] sideLiquidOut = new int [displayLiquidStages];	
		public int [] centerLiquidOut = new int [displayLiquidStages * 2];
	}
	
	private DisplayLiquidList displayLiquidLists[] = new DisplayLiquidList[Block.blocksList.length];
	
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
    	if (displayLiquidLists [liquidId] != null) {
    		return displayLiquidLists [liquidId];
    	}
    	
    	DisplayLiquidList d = new DisplayLiquidList();
    	displayLiquidLists [liquidId] = d;
    	
		BlockInterface block = new BlockInterface();
		block.texture = Block.blocksList [liquidId].blockIndexInTexture;//12 * 16 + 13;
		float size = Utils.pipeMaxPos - Utils.pipeMinPos;
		
    	for (int s = 0; s < displayLiquidStages * 2; ++s) {
    		if (s < displayLiquidStages) {
        		d.sideLiquidIn [s] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.sideLiquidIn [s], 4864 /*GL_COMPILE*/);    
        		
    			block.minX = 0;
    		} else {
    			d.sideLiquidOut [s - displayLiquidStages] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.sideLiquidOut [s - displayLiquidStages], 4864 /*GL_COMPILE*/);    
    			
    			block.minX = (s - (float) displayLiquidStages) / ((float) displayLiquidStages) * size / 2F;
    		}
    		
    		block.minY = Utils.pipeMinPos + 0.01;
    		block.minZ = Utils.pipeMinPos + 0.01;

    		if (s < displayLiquidStages) {
    			block.maxX = s / ((float) displayLiquidStages - 1) * size / 2F;
    		} else {
    			block.maxX = size / 2F;
    		}
    				
    		block.maxY = Utils.pipeMaxPos - 0.01;
    		block.maxZ = Utils.pipeMaxPos - 0.01;    		

    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);

    		GL11.glEndList();    		
    	}
        
    	for (int s = 0; s < displayLiquidStages * 4; ++s) {
    		if (s < displayLiquidStages * 2) {
        		d.centerLiquidIn [s] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.centerLiquidIn [s], 4864 /*GL_COMPILE*/);
        		
    			block.minX = Utils.pipeMinPos + 0.01F;
    		} else {
        		d.centerLiquidOut [s - displayLiquidStages * 2] = GLAllocation.generateDisplayLists(1);
        		GL11.glNewList(d.centerLiquidOut [s - displayLiquidStages * 2], 4864 /*GL_COMPILE*/);
    			
				block.minX = Utils.pipeMinPos + 0.01F
						+ (s - (float) displayLiquidStages * 2F - 1) / ((float) displayLiquidStages * 2F - 1)
						* size;
    		}
    		
    		block.minY = Utils.pipeMinPos + 0.01F;
    		block.minZ = Utils.pipeMinPos + 0.01F;
    		
    		if (s < displayLiquidStages * 2) {
				block.maxX = Utils.pipeMaxPos - ((size - s
				/ ((float) displayLiquidStages * 2F - 1) * size)) - 0.01;
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
    
    private void initializeDisplayPowerList () {    	
		BlockInterface block = new BlockInterface();
		block.texture = 0 * 16 + 4;
		
		float size = Utils.pipeMaxPos - Utils.pipeMinPos;
		
    	for (int s = 0; s < displayPowerStages; ++s) {
    		displayPowerList [s] = GLAllocation.generateDisplayLists(1); 
    		GL11.glNewList(displayPowerList [s], 4864 /*GL_COMPILE*/);
    		    			
    		float minSize = 0.005F;
    		
    		float unit = (size - minSize) / 2F / (float) displayPowerStages;
    		
    		block.minY = 0.5 - (minSize / 2F) - unit * s;
    		block.maxY = 0.5 + (minSize / 2F) + unit * s;
    		
    		block.minZ = 0.5 - (minSize / 2F) - unit * s;
    		block.maxZ = 0.5 + (minSize / 2F) + unit * s;
    		
    		block.minX = 0;    
    		block.maxX = 0.5 + (minSize / 2F) + unit * s;
    		    		
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
				
				GL11.glCallList(displayPowerList [stage]);
			} 
			
			GL11.glPopMatrix();		
		}
				
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();				
	}

	private void renderLiquids(Pipe pipe, double x, double y, double z) {
		PipeTransportLiquids liq = (PipeTransportLiquids) pipe.transport;
		
		if (liq.getLiquidId() == 0) {
			return;
		}
		
		Block block = Block.blocksList [liq.getLiquidId()];
		
		DisplayLiquidList d = getDisplayLiquidLists(liq.getLiquidId());
		
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
			renderSide (angleY [i], angleZ [i], liq.getSideToCenter(i), liq.getCenterToSide(i), d);
		}
				
		// CENTER

		if (liq.getCenterIn () > 0 || liq.getCenterOut () > 0) {
			renderCenter(liq.lastFromOrientation, liq.lastToOrientation,
					liq.getCenterIn(), liq.getCenterOut(), d);
		}
	
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();		
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
	
	private void renderSide(float angleY, float angleZ, float sideToCenter, float centerToSide, DisplayLiquidList d) {
		if (sideToCenter == 0 && centerToSide == 0) {
			return;
		}
				
		GL11.glPushMatrix();		
		
		GL11.glRotatef(angleY, 0, 1, 0);
		GL11.glRotatef(angleZ, 0, 0, 1);
		
		if (sideToCenter + centerToSide >= BuildCraftCore.BUCKET_VOLUME / 4) {
			GL11.glCallList(d.sideLiquidIn[displayLiquidStages - 1]);
		} else {
			if (sideToCenter > 0) {
				GL11.glCallList(d.sideLiquidIn[(int) (sideToCenter
						/ (BuildCraftCore.BUCKET_VOLUME / 4F) * (displayLiquidStages - 1))]);
			}

			if (centerToSide > 0) {
				GL11.glCallList(d.sideLiquidOut[(displayLiquidStages - 1)
						- (int) (centerToSide / (BuildCraftCore.BUCKET_VOLUME / 4F) * (displayLiquidStages - 1))]);
			}
		}

		GL11.glPopMatrix();
	}
	
	private void renderCenter(Orientations lastFromOrientation, Orientations lastToOrientation, float centerIn, float centerOut, DisplayLiquidList d) {
		GL11.glPushMatrix();					

		if (centerIn + centerOut >= BuildCraftCore.BUCKET_VOLUME / 2) {
			GL11.glRotatef(angleY [lastFromOrientation.ordinal()], 0, 1, 0);
			GL11.glRotatef(angleZ [lastFromOrientation.ordinal()], 0, 0, 1);
			GL11.glCallList(d.centerLiquidIn [displayLiquidStages * 2 - 1]);
		} else {
			if (centerIn > 0) {
				GL11.glRotatef(angleY [lastFromOrientation.ordinal()], 0, 1, 0);
				GL11.glRotatef(angleZ [lastFromOrientation.ordinal()], 0, 0, 1);
			} else {
				GL11.glRotatef(angleY [lastToOrientation.reverse().ordinal()], 0, 1, 0);
				GL11.glRotatef(angleZ [lastToOrientation.reverse().ordinal()], 0, 0, 1);
			}			
			
			if (centerIn > 0) {
				GL11.glCallList(d.centerLiquidIn[(int) (centerIn / (BuildCraftCore.BUCKET_VOLUME / 2F) * (displayLiquidStages * 2 - 1))]);
			}

			if (centerOut > 0) {
				GL11.glCallList(d.centerLiquidOut[(displayLiquidStages * 2 - 1)
						- (int) (centerOut
								/ (BuildCraftCore.BUCKET_VOLUME / 2F) * (displayLiquidStages * 2 - 1))]);
			}
		}

		GL11.glPopMatrix();
	}

	private void doRenderItem(EntityPassiveItem entityitem, double x, double y,
			double z, double brigntess) {
		if (entityitem == null || entityitem.item == null) {
			return;
		}

		ItemStack itemstack = entityitem.item;

		GL11.glPushMatrix();

		byte byte0 = 1;
		if (entityitem.item.stackSize > 1) {
			byte0 = 2;
		}
		if (entityitem.item.stackSize > 5) {
			byte0 = 3;
		}
		if (entityitem.item.stackSize > 20) {
			byte0 = 4;
		}

		GL11.glTranslatef((float) x, (float) y, (float) z);

		GL11.glEnable(32826 /* GL_RESCALE_NORMAL_EXT */);

		if (itemstack.itemID < Block.blocksList.length
				&& itemstack.itemID > 0
				&& RenderBlocks
						.renderItemIn3d(Block.blocksList[itemstack.itemID]
								.getRenderType())) {
			GL11.glTranslatef(0, 0.25F, 0);
			Block block = Block.blocksList[itemstack.itemID];

			if (block instanceof ITextureProvider) {
				MinecraftForgeClient.bindTexture(((ITextureProvider) block)
						.getTextureFile());
			} else {
				MinecraftForgeClient.bindTexture("/terrain.png");
			}

			float f4 = 0.25F;
			if (!Block.blocksList[itemstack.itemID].renderAsNormalBlock()
					&& itemstack.itemID != Block.stairSingle.blockID) {
				f4 = 0.5F;
			}
			GL11.glScalef(f4, f4, f4);
			for (int j = 0; j < byte0; j++) {
				GL11.glPushMatrix();
				renderBlocks.renderBlockOnInventory(
						Block.blocksList[itemstack.itemID],
						itemstack.getItemDamage(), (float) brigntess);
				GL11.glPopMatrix();
			}

		} else {
			GL11.glTranslatef(0, 0.10F, 0);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			int i = itemstack.getIconIndex();
			if (itemstack.getItem() instanceof ITextureProvider) {
				MinecraftForgeClient.bindTexture(((ITextureProvider) itemstack
						.getItem()).getTextureFile());
			} else if (itemstack.itemID < 256) {
				MinecraftForgeClient.bindTexture("/terrain.png");
			} else {
				MinecraftForgeClient.bindTexture("/gui/items.png");
			}
			Tessellator tessellator = Tessellator.instance;
			float f6 = (float) ((i % 16) * 16 + 0) / 256F;
			float f8 = (float) ((i % 16) * 16 + 16) / 256F;
			float f10 = (float) ((i / 16) * 16 + 0) / 256F;
			float f11 = (float) ((i / 16) * 16 + 16) / 256F;
			float f12 = 1.0F;
			float f13 = 0.5F;
			float f14 = 0.25F;
			for (int k = 0; k < byte0; k++) {
				GL11.glPushMatrix();

				GL11.glRotatef(180F - RenderManager.instance.playerViewY, 0.0F,
						1.0F, 0.0F);
				tessellator.startDrawingQuads();
				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.addVertexWithUV(0.0F - f13, 0.0F - f14, 0.0D, f6,
						f11);
				tessellator.addVertexWithUV(f12 - f13, 0.0F - f14, 0.0D, f8,
						f11);
				tessellator.addVertexWithUV(f12 - f13, 1.0F - f14, 0.0D, f8,
						f10);
				tessellator.addVertexWithUV(0.0F - f13, 1.0F - f14, 0.0D, f6,
						f10);
				tessellator.draw();
				GL11.glPopMatrix();
			}

		}
		GL11.glDisable(32826 /* GL_RESCALE_NORMAL_EXT */);
		GL11.glPopMatrix();
	}
}
