package net.minecraft.src.buildcraft.factory;

import java.util.HashMap;

import javax.rmi.CORBA.Tie;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftEnergy;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.Item;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.IInventoryRenderer;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock.BlockInterface;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

public class RenderRefinery extends TileEntitySpecialRenderer implements IInventoryRenderer {

	static final float factor = (float) (1.0 / 16.0);
	
	private ModelRenderer tank;
	private ModelRenderer magnet;
	
	public RenderRefinery () {

		//constructor:
		tank = new ModelRenderer(0, 0);
		tank.addBox(-4F, -8F, -4F, 8, 16, 8);		
		tank.rotationPointX = 8;
		tank.rotationPointY = 8;
		tank.rotationPointZ = 8;
		
		//constructor:
		magnet = new ModelRenderer(32, 0);
		magnet.addBox(0, -8F, -8F, 8, 4, 4);		
		magnet.rotationPointX = 8;
		magnet.rotationPointY = 8;
		magnet.rotationPointZ = 8;
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
		
		if (tile != null) {
			liquid1 = tile.slot1.liquidId;
			qty1 = tile.slot1.quantity;
			
			liquid2 = tile.slot2.liquidId;
			qty2 = tile.slot2.quantity;
		}
		
		GL11.glPushMatrix();
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		
		GL11.glTranslatef((float)x, (float)y, (float)z);
		
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
		
		magnet.render(factor);
		GL11.glTranslatef(0, 0, 12F * factor);
		magnet.render(factor);
		GL11.glTranslatef(0, 0, -12F * factor);
		
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
		setTextureFor(BuildCraftEnergy.fuel.shiftedIndex);
		GL11.glCallList(getDisplayLists(BuildCraftEnergy.fuel.shiftedIndex)[20]);
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
