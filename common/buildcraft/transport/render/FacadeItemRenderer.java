/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import buildcraft.BuildCraftTransport;
import buildcraft.core.CoreConstants;
import buildcraft.core.render.RenderUtils;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.PipeIconProvider;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class FacadeItemRenderer implements IItemRenderer {

	private static long lastCycle = 0L;

	public boolean renderToggle = false;

	private void renderFacadeItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		if (lastCycle < System.currentTimeMillis()) {
			renderToggle = !renderToggle;
			lastCycle = System.currentTimeMillis() + 1000L;
		}

		int decodedMeta = ItemFacade.getMetaData(item);
		int decodedMeta_alt = ItemFacade.getAlternateMetaData(item);
		Block block = ItemFacade.getBlock(item);
		Block block_alt = ItemFacade.getAlternateBlock(item);

		int actual_meta = decodedMeta;
		Block actual_block = block;

		if (block_alt != null) {
			actual_meta = renderToggle ? decodedMeta : decodedMeta_alt;
			actual_block = renderToggle ? block : block_alt;
		}

		try {
			int color = item.getItem().getColorFromItemStack(new ItemStack(block, 1, decodedMeta), 0);
			RenderUtils.setGLColorFromInt(color);
		} catch (Throwable error) {
		}

		Tessellator tessellator = Tessellator.instance;

		if (block == null) {
			return;
		}

		if(block.getIcon(0, decodedMeta) == null) {
			return;
		}

		// Render Facade
		GL11.glPushMatrix();

	        // Enable glBlending for transparency
	    	if (block.getRenderBlockPass() > 0) {
	    		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
	    		GL11.glEnable(GL11.GL_BLEND);
	    		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
	    	}

		block.setBlockBounds(0F, 0F, 1F - 1F / 16F, 1F, 1F, 1F);
		render.setRenderBoundsFromBlock(block);
		GL11.glTranslatef(translateX, translateY, translateZ);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, tryGetBlockIcon(actual_block, 0, actual_meta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, tryGetBlockIcon(actual_block, 1, actual_meta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, tryGetBlockIcon(actual_block, 2, actual_meta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, tryGetBlockIcon(actual_block, 3, actual_meta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, tryGetBlockIcon(actual_block, 4, actual_meta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, tryGetBlockIcon(actual_block, 5, actual_meta));
		tessellator.draw();
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

	        // Disable blending
	    	if (block.getRenderBlockPass() > 0) {
	    		GL11.glDisable(GL11.GL_BLEND);
	    	}

		GL11.glPopMatrix();

		// Render StructurePipe
		block = BuildCraftTransport.genericPipeBlock;
		IIcon textureID = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure pipe

		block.setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS - 1F / 16F);
		block.setBlockBoundsForItemRender();
		render.setRenderBoundsFromBlock(block);
		GL11.glTranslatef(translateX, translateY, translateZ + 0.25F);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -0F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	private IIcon tryGetBlockIcon( Block block, int side, int decodedMeta)
	{
		try {
		return block.getIcon(side, decodedMeta);
		} catch (Throwable t) {
			return Blocks.cobblestone.getIcon(0, 0);
		}
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch (type) {
			case ENTITY:
				return true;
			case EQUIPPED:
				return true;
			case EQUIPPED_FIRST_PERSON:
				return true;
			case INVENTORY:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {

		switch (type) {
			case ENTITY:
				GL11.glScalef(0.50F, 0.50F, 0.50F);
				renderFacadeItem((RenderBlocks) data[0], item, -0.6F, 0f, -0.6F);
				break;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
				renderFacadeItem((RenderBlocks) data[0], item, 0F, 0F, 0f);
				break;
			case INVENTORY:
				GL11.glScalef(1.1F, 1.1F, 1.1F);
				renderFacadeItem((RenderBlocks) data[0], item, -0.3f, -0.35f, -0.7f);
				break;
			default:
		}
	}
}
