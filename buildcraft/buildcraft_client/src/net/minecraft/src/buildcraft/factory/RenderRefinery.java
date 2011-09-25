package net.minecraft.src.buildcraft.factory;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.Item;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.core.IInventoryRenderer;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock.BlockInterface;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

public class RenderRefinery extends TileEntitySpecialRenderer implements IInventoryRenderer {

	static final float factor = (float) (1.0 / 16.0);
	
	private ModelRenderer tank;
	private ModelRenderer magnet [] = new ModelRenderer [4];
	
	private ModelBase model = new ModelBase () {};
	
	public RenderRefinery () {

		//constructor:
		tank = new ModelRenderer(model, 0, 0);
		tank.addBox(-4F, -8F, -4F, 8, 16, 8);		
		tank.rotationPointX = 8;
		tank.rotationPointY = 8;
		tank.rotationPointZ = 8;
		
		//constructor:
		
		for (int i = 0; i < 4; ++i) {
			magnet[i] = new ModelRenderer(model, 32, i * 8);
			magnet[i].addBox(0, -8F, -8F, 8, 4, 4);		
			magnet[i].rotationPointX = 8;
			magnet[i].rotationPointY = 8;
			magnet[i].rotationPointZ = 8;
			
		}
		
	}
	
final static private int displayStages = 100;
	
	private HashMap <Integer, int []> stage = new HashMap <Integer, int []> ();	
	
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
    		
    		block.minX = 0.5 - 4F * factor + 0.01;
    		block.minY = 0;
    		block.minZ = 0.5 - 4F * factor + 0.01;
    		
    		block.maxX = 0.5 + 4F * factor - 0.01;
    		block.maxY = (float) s / (float) displayStages;
    		block.maxZ = 0.5 + 4F * factor - 0.01;
    		
    		RenderEntityBlock.renderBlock(block, APIProxy.getWorld(), 0,
    				0, 0, false);
    		
    		GL11.glEndList();
    	}
    	
    	return d;
    }
	
	public RenderRefinery (String baseTexture) {
		this ();
	}

	public void inventoryRender(double x, double y, double z,
			float f, float f1) {
		render(null, x, y, z);
	}
	

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {
	
		render((TileRefinery) tileentity, x, y, z);
	}
	
	private void render(TileRefinery tile, double x, double y, double z) {
			
		int liquid1 = 0, liquid2 = 0, liquid3 = 0;
		int qty1 = 0, qty2 = 0, qty3 = 0;
		float anim = 0;
		int angle = 0;
		ModelRenderer theMagnet = magnet [0];
		
		if (tile != null) {
			liquid1 = tile.slot1.liquidId;
			qty1 = tile.slot1.quantity;
			
			liquid2 = tile.slot2.liquidId;
			qty2 = tile.slot2.quantity;
			
			liquid3 = tile.result.liquidId;
			qty3 = tile.result.quantity;
			
			anim = tile.getAnimationStage();
			
			switch (tile.worldObj.getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord)) {
			case 2:
				angle = 90;
				break;
			case 3:
				angle = 270;
				break;
			case 4:
				angle = 180;
				break;
			case 5:
				angle = 0;
				break;
			}
	
			if (tile.animationSpeed <= 1) {
				theMagnet = magnet [0];
			} else if (tile.animationSpeed <= 2.5) {
				theMagnet = magnet [1];
			} else if (tile.animationSpeed <= 4.5) {
				theMagnet = magnet [2];
			} else {
				theMagnet = magnet [3];
			}
			
		}
		
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);
				
		GL11.glTranslatef((float)x, (float)y, (float)z);
		
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		GL11.glRotatef(angle, 0, 1, 0);
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		
		MinecraftForgeClient.bindTexture("/net/minecraft/src/buildcraft/factory/gui/refinery.png");
		GL11.glTranslatef(-4F * factor, 0, -4F * factor);
		tank.render(factor);
		GL11.glTranslatef(4F * factor, 0, 4F * factor);
		
		GL11.glTranslatef(-4F * factor, 0, 4F * factor);
		tank.render(factor);
		GL11.glTranslatef(4F * factor, 0, -4F * factor);
				
		GL11.glTranslatef(4F * factor, 0, 0);
		tank.render(factor);
		GL11.glTranslatef(-4F * factor, 0, 0);
			
		float trans1, trans2;
		
		if (anim <= 100) {
			trans1 = 12F * factor * anim / 100F;
			trans2 = 0;
		} else if (anim <= 200) {
			trans1 = 12F * factor - (12F * factor * (anim - 100F) / 100F);
			trans2 = 12F * factor * (anim - 100F) / 100F;
		} else {
			trans1 = 12F * factor * (anim - 200F) / 100F;
			trans2 = 12F * factor - (12F * factor * (anim - 200F) / 100F);			
		}
		
		GL11.glTranslatef(0, trans1, 0);	
		theMagnet.render(factor);
		GL11.glTranslatef(0, -trans1, 0);
		
		GL11.glTranslatef(0, trans2, 12F * factor);		
		theMagnet.render(factor);
		GL11.glTranslatef(0, -trans2, -12F * factor);
		
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		
		GL11.glTranslatef(-4F * factor, 0, -4F * factor);
		if (qty1 > 0) {
			setTextureFor(liquid1);
			GL11.glCallList(getDisplayLists(liquid1)[(int) ((float) qty1
					/ (float) TileRefinery.LIQUID_PER_SLOT * (float) (displayStages - 1))]);	
		}
		GL11.glTranslatef(4F * factor, 0, 4F * factor);
		
		GL11.glTranslatef(-4F * factor, 0, 4F * factor);
		if (qty2 > 0) {
			setTextureFor(liquid2);
			GL11.glCallList(getDisplayLists(liquid2)[(int) ((float) qty2
					/ (float) TileRefinery.LIQUID_PER_SLOT * (float) (displayStages - 1))]);	
		}
		GL11.glTranslatef(4F * factor, 0, -4F * factor);

		GL11.glTranslatef(4F * factor, 0, 0);
		if (qty3 > 0) {
			setTextureFor(liquid3);
			GL11.glCallList(getDisplayLists(liquid3)[(int) ((float) qty3
					/ (float) TileRefinery.LIQUID_PER_SLOT * (float) (displayStages - 1))]);	
		}
		GL11.glTranslatef(-4F * factor, 0, 0);
		
		
		
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();
	}	
	
	public void setTextureFor(int liquidId) {
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
	}
}
