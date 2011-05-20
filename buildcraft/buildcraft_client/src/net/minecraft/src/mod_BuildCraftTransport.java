package net.minecraft.src;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.buildcraft.core.IPipeConnection;
import net.minecraft.src.buildcraft.core.Orientations;
import net.minecraft.src.buildcraft.core.Utils;

public class mod_BuildCraftTransport extends BaseMod {
	
	public void ModsLoaded () {
		super.ModsLoaded();
		BuildCraftTransport.ModsLoaded();
		BuildCraftTransport.initializeModel(this);		
	}
	
		
	@Override
	public String Version() {
		return "1.5_01.5";
	}
	
	public boolean RenderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
		
    	if (block.getRenderType() == BuildCraftTransport.pipeModel) {
    		float minSize = Utils.pipeMinSize;
    		float maxSize = Utils.pipeMaxSize;
    		int initialTexture = block.blockIndexInTexture;
    		
    		block.setBlockBounds(minSize, minSize, minSize, maxSize, maxSize, maxSize);
    		renderblocks.renderStandardBlock(block, i, j, k);
    		
    		int metadata = iblockaccess.getBlockMetadata(i, j, k);
    		
    		IPipeConnection connect = (IPipeConnection) block;
    		
			if (connect.isPipeConnected(iblockaccess, i - 1, j, k)) {
				if (block == BuildCraftTransport.ironPipeBlock
						&& metadata != Orientations.XNeg.ordinal()) {
					block.blockIndexInTexture = BuildCraftTransport.plainIronTexture;
				} else if (block == BuildCraftTransport.diamondPipeBlock) {
					block.blockIndexInTexture = BuildCraftTransport.diamondTextures[Orientations.XNeg
							.ordinal()];
				}
				block.setBlockBounds(0.0F, minSize, minSize, minSize, maxSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i + 1, j, k)) {
				if (block == BuildCraftTransport.ironPipeBlock
						&& metadata != Orientations.XPos.ordinal()) {
					block.blockIndexInTexture = BuildCraftTransport.plainIronTexture;
				} else if (block == BuildCraftTransport.diamondPipeBlock) {
					block.blockIndexInTexture = BuildCraftTransport.diamondTextures[Orientations.XPos
							.ordinal()];
				}
				block.setBlockBounds(maxSize, minSize, minSize, 1.0F, maxSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j - 1, k)) {
				if (block == BuildCraftTransport.ironPipeBlock
						&& metadata != Orientations.YNeg.ordinal()) {
					block.blockIndexInTexture = BuildCraftTransport.plainIronTexture;
				} else if (block == BuildCraftTransport.diamondPipeBlock) {
					block.blockIndexInTexture = BuildCraftTransport.diamondTextures[Orientations.YNeg
							.ordinal()];
				}
				block.setBlockBounds(minSize, 0.0F, minSize, maxSize, minSize,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j + 1, k)) {
				if (block == BuildCraftTransport.ironPipeBlock
						&& metadata != Orientations.YPos.ordinal()) {
					block.blockIndexInTexture = BuildCraftTransport.plainIronTexture;
				} else if (block == BuildCraftTransport.diamondPipeBlock) {
					block.blockIndexInTexture = BuildCraftTransport.diamondTextures[Orientations.YPos
							.ordinal()];
				}
				block.setBlockBounds(minSize, maxSize, minSize, maxSize, 1.0F,
						maxSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j, k - 1)) {
				if (block == BuildCraftTransport.ironPipeBlock
						&& metadata != Orientations.ZNeg.ordinal()) {
					block.blockIndexInTexture = BuildCraftTransport.plainIronTexture;
				} else if (block == BuildCraftTransport.diamondPipeBlock) {
					block.blockIndexInTexture = BuildCraftTransport.diamondTextures[Orientations.ZNeg
							.ordinal()];
				}
				block.setBlockBounds(minSize, minSize, 0.0F, maxSize, maxSize,
						minSize);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
			if (connect.isPipeConnected(iblockaccess, i, j, k + 1)) {
				if (block == BuildCraftTransport.ironPipeBlock
						&& metadata != Orientations.ZPos.ordinal()) {
					block.blockIndexInTexture = BuildCraftTransport.plainIronTexture;
				} else if (block == BuildCraftTransport.diamondPipeBlock) {
					block.blockIndexInTexture = BuildCraftTransport.diamondTextures[Orientations.ZPos
							.ordinal()];
				}
				block.setBlockBounds(minSize, minSize, maxSize, maxSize,
						maxSize, 1.0F);
				renderblocks.renderStandardBlock(block, i, j, k);
				block.blockIndexInTexture = initialTexture;
			}
    		
    		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    		
    		return true;
    	} 
    	
        return false;
    }
    
    public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j)
    {
		if (block.getRenderType() == BuildCraftTransport.pipeModel) {			
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
    }
	    

}
