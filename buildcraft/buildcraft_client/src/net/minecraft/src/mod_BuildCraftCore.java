package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.core.EntityBlock;
import net.minecraft.src.buildcraft.core.RenderEntityBlock;

public class mod_BuildCraftCore extends BaseMod {	
	
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
		return "1.5_01.5";
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	// map.put (EntityPassiveItem.class, new RenderItem());
    	map.put (EntityPassiveItem.class, new RenderPassiveItem());    	
    	map.put (EntityBlock.class, new RenderEntityBlock());
    }
    
    public boolean RenderWorldBlock(RenderBlocks renderblocks,
			IBlockAccess iblockaccess, int i, int j, int k, Block block, int l)
    {
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel) {
			renderblocks.renderStandardBlock(block, i, j, k);
			
			return true;
		} else if (block.getRenderType() == BuildCraftCore.customTextureModel) {
			Tessellator tessellator = Tessellator.instance;    		
    		tessellator.draw();
    		tessellator.startDrawingQuads();
    		
			GL11.glBindTexture(
					3553 /* GL_TEXTURE_2D */,
					ModLoader.getMinecraftInstance().renderEngine
							.getTexture("/net/minecraft/src/buildcraft/core/gui/buildcraft_terrain.png"));
			
			renderblocks.renderStandardBlock(block, i, j, k);
			
    		tessellator.draw();
    		tessellator.startDrawingQuads();
    		
    		GL11.glBindTexture(
					3553 /* GL_TEXTURE_2D */,
					ModLoader.getMinecraftInstance().renderEngine
							.getTexture("/terrain.png"));
		}
		
		return false;
    }
	
    public void RenderInvBlock(RenderBlocks renderblocks, Block block, int i, int j) {
		if (block.getRenderType() == BuildCraftCore.blockByEntityModel
				&& blockByEntityRenders.containsKey(block)) {
    		//  ??? GET THE ENTITY FROM THE TILE
			blockByEntityRenders.get(block).doRender(null, -0.5, -0.5, -0.5, 0,
					0);
		} else if (block.getRenderType() == BuildCraftCore.customTextureModel) {    		
			GL11.glBindTexture(
					3553 /* GL_TEXTURE_2D */,
					ModLoader.getMinecraftInstance().renderEngine
							.getTexture("/net/minecraft/src/buildcraft/core/gui/buildcraft_terrain.png"));
			
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
    		
    		GL11.glBindTexture(
					3553 /* GL_TEXTURE_2D */,
					ModLoader.getMinecraftInstance().renderEngine
							.getTexture("/terrain.png"));
		}
    }
}
