package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.ICustomTextureBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;
import net.minecraft.src.buildcraft.core.Utils;

public class mod_BuildCraftCore extends BaseModMp {	
	
	BuildCraftCore proxy = new BuildCraftCore();
	
	public static HashMap<Block, Render> blockByEntityRenders = 
		new HashMap<Block, Render>();
		
	public static void initialize () {
		BuildCraftCore.initialize ();	
	}
		
	public void ModsLoaded () {
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeModel(this);
	}
	
	@Override
	public String Version() {
		return "1.6.6.2";
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	map.put (EntityPassiveItem.class, new RenderPassiveItem());    	
    	map.put (EntityBlock.class, new RenderEntityBlock());
    }
    
    public boolean RenderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
    	if (block instanceof ICustomTextureBlock) {
    		Tessellator tessellator = Tessellator.instance;    		
    		tessellator.draw();
    		tessellator.startDrawingQuads();
    		
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture(((ICustomTextureBlock) (block))
							.getTextureFile()));
    	}
    	
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {
			renderblocks.renderStandardBlock(block, i, j, k);
			
			return true;
		} else if (block.getRenderType() == BuildCraftCore.customTextureModel) {
			renderblocks.renderStandardBlock(block, i, j, k);
		} else if (block.getRenderType() == BuildCraftCore.pipeModel) {    
    		float minSize = Utils.pipeMinSize;
    		float maxSize = Utils.pipeMaxSize;
    		int initialTexture = block.blockIndexInTexture;
    		
    		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
    		renderblocks.renderStandardBlock(block, i, j, k);
    		
    		int metadata = iblockaccess.getBlockMetadata(i, j, k);
    		
    		IPipeConnection connect = (IPipeConnection) block;
    		
			if (connect.isPipeConnected(iblockaccess, i - 1, j, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
						.getTextureForConnection(Orientations.XNeg, metadata);
				block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i + 1, j, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
				.getTextureForConnection(Orientations.XPos, metadata);
				block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j - 1, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
				.getTextureForConnection(Orientations.YNeg, metadata);
				block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j + 1, k)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
				.getTextureForConnection(Orientations.YPos, metadata);
				block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j, k - 1)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
				.getTextureForConnection(Orientations.ZNeg, metadata);
				block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize,
						minSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j, k + 1)) {
				block.blockIndexInTexture = ((IBlockPipe) block)
				.getTextureForConnection(Orientations.ZPos, metadata);
				block.setBlockBounds(minSize, minSize, maxSize, maxSize,
						maxSize, 1.0F);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
    		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    	} 
		
    	if (block instanceof ICustomTextureBlock) {
    		Tessellator tessellator = Tessellator.instance;    	
    		tessellator.draw();
    		tessellator.startDrawingQuads();
    		
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture("/terrain.png"));
			
			return true;
    	}
		
		return false;
    }
	
    public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j) {
    	if (block instanceof ICustomTextureBlock) {
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture(((ICustomTextureBlock) (block))
							.getTextureFile()));
    	}    	
    	
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel
				&& blockByEntityRenders.containsKey(block)) {
    		//  ??? GET THE ENTITY FROM THE TILE
			blockByEntityRenders.get(block).doRender(null, -0.5, -0.5, -0.5, 0,
					0);
		} else if (block.getRenderType() == BuildCraftCore.customTextureModel) {    		
			Tessellator tessellator = Tessellator.instance;    		
			block.setBlockBoundsForItemRender();
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1F, 0.0F);
            renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(0, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(1, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1F);
            renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(2, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(3, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1F, 0.0F, 0.0F);
            renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(4, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(5, i));
            tessellator.draw();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		} else if (block.getRenderType() == BuildCraftCore.pipeModel) {			
    		Tessellator tessellator = Tessellator.instance;    		

    		block.setBlockBounds(Utils.pipeMinSize, 0.0F, Utils.pipeMinSize, Utils.pipeMaxSize, 1.0F, Utils.pipeMaxSize);
            block.setBlockBoundsForItemRender();
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, -1F, 0.0F);
            renderblocks.renderBottomFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(0, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderblocks.renderTopFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(1, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1F);
            renderblocks.renderEastFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(2, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderblocks.renderWestFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(3, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(-1F, 0.0F, 0.0F);
            renderblocks.renderNorthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(4, i));
            tessellator.draw();
            tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderblocks.renderSouthFace(block, 0.0D, 0.0D, 0.0D, block.getBlockTextureFromSideAndMetadata(5, i));
            tessellator.draw();
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    	}
		
    	if (block instanceof ICustomTextureBlock) {
			GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
					.getMinecraftInstance().renderEngine
					.getTexture("/terrain.png"));
    	}
    }
}
